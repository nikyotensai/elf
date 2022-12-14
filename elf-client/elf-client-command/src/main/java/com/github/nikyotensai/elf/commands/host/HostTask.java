/*
 * Copyright (C) 2019 Qunar, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.nikyotensai.elf.commands.host;


import java.io.File;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nikyotensai.elf.agent.common.ResponseHandler;
import com.github.nikyotensai.elf.agent.common.job.BytesJob;
import com.github.nikyotensai.elf.agent.common.job.ContinueResponseJob;
import com.github.nikyotensai.elf.commands.perf.PerfData;
import com.github.nikyotensai.elf.common.FileUtil;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.remoting.netty.AgentRemotingExecutor;
import com.github.nikyotensai.elf.remoting.netty.Task;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import com.sun.management.OperatingSystemMXBean;

import sun.management.counter.Counter;

/**
 * @author leix.xie
 * @since 2018/11/15 15:12
 */
public class HostTask implements Task {

    private static final Logger logger = LoggerFactory.getLogger(HostTask.class);

    private static final Joiner SPACE_JOINER = Joiner.on(" ").skipNulls();

    private static final String LOADAVG_FILENAME = "/proc/loadavg";

    private static final short KB = 1024;

    private final String id;

    private final int pid;

    private final ResponseHandler handler;

    private final long maxRunningMs;

    private final SettableFuture<Integer> future = SettableFuture.create();

    public HostTask(String id, int pid, ResponseHandler handler, long maxRunningMs) {
        this.id = id;
        this.pid = pid;
        this.handler = handler;
        this.maxRunningMs = maxRunningMs;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getMaxRunningMs() {
        return maxRunningMs;
    }

    @Override
    public ContinueResponseJob createJob() {
        return new Job();
    }

    @Override
    public ListenableFuture<Integer> getResultFuture() {
        return future;
    }

    private class Job extends BytesJob {

        private Job() {
            super(id, handler, future);
        }

        @Override
        protected byte[] getBytes() throws Exception {
            VirtualMachineUtil.VMConnector connect = VirtualMachineUtil.connect(pid);
            MxBean mxBean = new MxBean(getCounters(pid),
                    connect.getRuntimeMXBean(),
                    connect.getOperatingSystemMXBean(),
                    connect.getMemoryMXBean(),
                    connect.getThreadMXBean(),
                    connect.getClassLoadingMXBean(),
                    connect.getGarbageCollectorMXBeans(),
                    connect.getMemoryPoolMXBeans());

            Map<String, Object> result = new HashMap<>();
            result.put("type", "hostInfo");
            result.put("jvm", getJvmInfo(mxBean));
            result.put("host", getHostInfo(mxBean));
            result.put("memPool", getMemoryPoolMXBeansInfo(mxBean.getMemoryPoolMXBeans()));
            result.put("visuaGC", getVisuaGCInfo(mxBean.getCounters()));
            return JacksonSerializer.serializeToBytes(result);
        }

        @Override
        public ListeningExecutorService getExecutor() {
            return AgentRemotingExecutor.getExecutor();
        }
    }

    private Map<String, Counter> getCounters(int pid) {
        try {
            PerfData prefData = PerfData.connect(pid);
            return prefData.getAllCounters();
        } catch (Exception e) {
            logger.warn("get perf counters error", e);
            return ImmutableMap.of();
        }
    }

    private static List<MemoryPoolInfo> getMemoryPoolMXBeansInfo(List<MemoryPoolMXBean> memoryPoolMXBeans) {
        List<MemoryPoolInfo> memoryPoolInfos = new ArrayList<>();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            MemoryUsage usage = memoryPoolMXBean.getUsage();
            MemoryPoolInfo info = new MemoryPoolInfo(memoryPoolMXBean.getName().replaceAll("^PS|^G1|\\s|\\'|-", ""), usage.getInit() / KB, usage.getUsed() / KB, usage.getCommitted() / KB, usage.getMax() / KB);
            memoryPoolInfos.add(info);
        }
        return memoryPoolInfos;
    }

    private VMSnapshotBean getVisuaGCInfo(Map<String, Counter> counters) {
        VMSnapshotBean vmSnapshotBean = new VMSnapshotBean();
        vmSnapshotBean.setEdenSize(getValue(counters, "sun.gc.generation.0.space.0.maxCapacity") / KB);
        vmSnapshotBean.setEdenCapacity(getValue(counters, "sun.gc.generation.0.space.0.capacity") / KB);
        vmSnapshotBean.setEdenUsed(getValue(counters, "sun.gc.generation.0.space.0.used") / KB);
        vmSnapshotBean.setEdenGCEvents(getValue(counters, "sun.gc.collector.0.invocations"));
        vmSnapshotBean.setEdenGCTime(getValue(counters, "sun.gc.collector.0.time"));

        vmSnapshotBean.setSurvivor0Size(getValue(counters, "sun.gc.generation.0.space.1.maxCapacity") / KB);
        vmSnapshotBean.setSurvivor0Capacity(getValue(counters, "sun.gc.generation.0.space.1.capacity") / KB);
        vmSnapshotBean.setSurvivor0Used(getValue(counters, "sun.gc.generation.0.space.1.used") / KB);

        vmSnapshotBean.setSurvivor1Size(getValue(counters, "sun.gc.generation.0.space.2.maxCapacity") / KB);
        vmSnapshotBean.setSurvivor1Capacity(getValue(counters, "sun.gc.generation.0.space.2.capacity") / KB);
        vmSnapshotBean.setSurvivor1Used(getValue(counters, "sun.gc.generation.0.space.2.used") / KB);

        vmSnapshotBean.setTenuredSize(getValue(counters, "sun.gc.generation.1.space.0.maxCapacity") / KB);
        vmSnapshotBean.setTenuredCapacity(getValue(counters, "sun.gc.generation.1.space.0.capacity") / KB);
        vmSnapshotBean.setTenuredUsed(getValue(counters, "sun.gc.generation.1.space.0.used") / KB);
        vmSnapshotBean.setTenuredGCEvents(getValue(counters, "sun.gc.collector.1.invocations"));
        vmSnapshotBean.setTenuredGCTime(getValue(counters, "sun.gc.collector.1.time"));

        vmSnapshotBean.setPermSize(getValue(counters, "sun.gc.generation.2.space.0.maxCapacity") / KB);
        vmSnapshotBean.setPermCapacity(getValue(counters, "sun.gc.generation.2.space.0.capacity") / KB);
        vmSnapshotBean.setPermUsed(getValue(counters, "sun.gc.generation.2.space.0.used") / KB);

        vmSnapshotBean.setMetaSize(getValue(counters, "sun.gc.metaspace.maxCapacity") / KB);
        vmSnapshotBean.setMetaCapacity(getValue(counters, "sun.gc.metaspace.capacity") / KB);
        vmSnapshotBean.setMetaUsed(getValue(counters, "sun.gc.metaspace.used") / KB);

        vmSnapshotBean.setClassLoadTime(getValue(counters, "sun.cls.time"));
        vmSnapshotBean.setClassesLoaded(getValue(counters, "java.cls.loadedClasses"));
        vmSnapshotBean.setClassesUnloaded(getValue(counters, "java.cls.unloadedClasses"));

        vmSnapshotBean.setTotalCompileTime(getValue(counters, "java.ci.totalTime"));
        vmSnapshotBean.setTotalCompile(getValue(counters, "sun.ci.totalCompiles"));

        vmSnapshotBean.setLastGCCause(getStringValue(counters, "sun.gc.lastCause"));

        return vmSnapshotBean;
    }

    private Long getValue(Map<String, Counter> counters, String key) {
        Counter counter = counters.get(key);
        if (counter != null && counter.getValue() != null) {
            return Long.valueOf(counter.getValue().toString());
        }
        return 0L;
    }

    private static String getStringValue(Map<String, Counter> counters, String key) {
        Counter counter = counters.get(key);
        if (counter != null && counter.getValue() != null) {
            return counter.getValue().toString();
        }
        return "";
    }

    private static VMSummaryInfo getJvmInfo(MxBean mxBean) {
        Map<String, Counter> counters = mxBean.getCounters();
        RuntimeMXBean runtimeBean = mxBean.getRuntimeBean();
        OperatingSystemMXBean osBean = mxBean.getOsBean();
        ThreadMXBean threadBean = mxBean.getThreadBean();
        ClassLoadingMXBean classLoadingBean = mxBean.getClassLoadingBean();
        MemoryMXBean memoryMXBean = mxBean.getMemoryMXBean();

        VMSummaryInfo vmInfo = new VMSummaryInfo();

        //????????????
        vmInfo.setUpTime(runtimeBean.getUptime());
        //??????CPU??????
        vmInfo.setProcessCpuTime(osBean.getProcessCpuTime() / 1000000);
        //????????????
        vmInfo.setOs(osBean.getName());
        //????????????
        vmInfo.setOsArch(osBean.getArch());
        //cpu??????
        vmInfo.setAvailableProcessors(osBean.getAvailableProcessors());
        //??????????????????
        vmInfo.setCommitedVirtualMemory(osBean.getCommittedVirtualMemorySize() / KB);
        //???????????????
        vmInfo.setTotalPhysicalMemorySize(osBean.getTotalPhysicalMemorySize() / KB);
        //??????????????????
        vmInfo.setFreePhysicalMemorySize(osBean.getFreePhysicalMemorySize() / KB);
        //???????????????
        vmInfo.setTotalSwapSpaceSize(osBean.getTotalSwapSpaceSize() / KB);
        //??????????????????
        vmInfo.setFreeSwapSpaceSize(osBean.getFreeSwapSpaceSize() / KB);

        //????????????JIT?????????
        vmInfo.setVmName(runtimeBean.getVmName());
        vmInfo.setJitCompiler(runtimeBean.getVmName());
        //?????????
        vmInfo.setVmVendor(runtimeBean.getVmVendor());
        //JDK??????
        vmInfo.setJdkVersion(getStringValue(counters, "java.property.java.version"));
        //vm??????
        vmInfo.setVmVersion(runtimeBean.getVmVersion());
        //????????????
        vmInfo.setCurrentThreadCount(threadBean.getThreadCount());
        //????????????
        vmInfo.setPeakThreadCount(threadBean.getPeakThreadCount());
        //???????????????
        vmInfo.setDaemonThreadCount(threadBean.getDaemonThreadCount());
        //??????????????????
        vmInfo.setTotalStartedThreadCount(threadBean.getTotalStartedThreadCount());

        //??????????????????
        vmInfo.setLoadedClassCount(classLoadingBean.getLoadedClassCount());
        //??????????????????
        vmInfo.setTotalLoadedClassCount(classLoadingBean.getTotalLoadedClassCount());
        //??????????????????
        vmInfo.setUnloadedClassCount(classLoadingBean.getUnloadedClassCount());

        //heap??????
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
        //???????????????
        vmInfo.setHeapCommitedMemory(memoryUsage.getCommitted() / KB);
        //???????????????
        vmInfo.setHeapUsedMemory(memoryUsage.getUsed() / KB);
        //???????????????
        vmInfo.setHeapMaxMemory(memoryUsage.getMax() / KB);

        memoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        //??????????????????
        vmInfo.setNonHeapCommitedMemory(memoryUsage.getCommitted() / KB);
        //??????????????????
        vmInfo.setNonHeapUsedMemory(memoryUsage.getUsed() / KB);
        //??????????????????
        vmInfo.setNonHeapMaxMemory(memoryUsage.getMax() / KB);

        //???????????????
        vmInfo.setGcInfos(getGCInfo(mxBean));

        //jvm??????
        vmInfo.setVmOptions(SPACE_JOINER.join(runtimeBean.getInputArguments()));
        //?????????
        vmInfo.setClassPath(runtimeBean.getClassPath());
        //?????????
        vmInfo.setLibraryPath(runtimeBean.getLibraryPath());
        //???????????????
        try {
            vmInfo.setBootClassPath(runtimeBean.getBootClassPath());
        } catch (Exception e) {
            //jdk9????????????????????? UnsupportedOperationException?????????
        }

        return vmInfo;
    }

    private static List<String> getGCInfo(MxBean mxBean) {
        List<GarbageCollectorMXBean> gcMxBeans = mxBean.getGcMxBeans();
        List<String> gcInfos = new ArrayList<>(gcMxBeans.size());
        for (GarbageCollectorMXBean b : gcMxBeans) {
            GCBean gcBean = new GCBean();
            gcBean.name = b.getName();
            gcBean.gcCount = b.getCollectionCount();
            gcBean.gcTime = b.getCollectionTime();
            gcInfos.add(gcBean.toString());
        }
        return gcInfos;
    }

    private static HostInfo getHostInfo(MxBean mxBean) {
        OperatingSystemMXBean osBean = mxBean.getOsBean();
        ThreadMXBean threadBean = mxBean.getThreadBean();

        HostInfo hostInfo = new HostInfo();
        String osName = System.getProperty("os.name");
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        String cpuLoadAverages = null;
        if (osName != null && osName.toLowerCase().contains("linux")) {
            try {
                File file = new File(LOADAVG_FILENAME);
                cpuLoadAverages = FileUtil.readFile(file);
            } catch (IOException e) {
                logger.error("get CPU Load Averages error", e);
            }
        }

        double systemLoadAverage = osBean.getSystemLoadAverage();
        // ???????????????
        long totalMemory = Runtime.getRuntime().totalMemory() / KB;
        // ????????????
        long freeMemory = Runtime.getRuntime().freeMemory() / KB;
        // ?????????????????????
        long maxMemory = Runtime.getRuntime().maxMemory() / KB;

        //???????????????
        long totalSwapSpaceSize = osBean.getTotalSwapSpaceSize() / KB;
        //??????????????????
        long freeSwapSpaceSize = osBean.getFreeSwapSpaceSize() / KB;
        //???????????????
        long totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize() / KB;
        //??????????????????
        long freePhysicalMemorySize = osBean.getFreePhysicalMemorySize() / KB;
        //??????CPU?????????
        double systemCpuLoad = osBean.getSystemCpuLoad();

        long totalThread = threadBean.getTotalStartedThreadCount();

        //??????????????????
        getDiskInfo(hostInfo);

        hostInfo.setAvailableProcessors(availableProcessors);
        hostInfo.setSystemLoadAverage(systemLoadAverage);
        hostInfo.setCpuLoadAverages(Strings.nullToEmpty(cpuLoadAverages));
        hostInfo.setOsName(osName);
        hostInfo.setTotalMemory(totalMemory);
        hostInfo.setFreeMemory(freeMemory);
        hostInfo.setMaxMemory(maxMemory);
        hostInfo.setTotalSwapSpaceSize(totalSwapSpaceSize);
        hostInfo.setFreeSwapSpaceSize(freeSwapSpaceSize);
        hostInfo.setTotalPhysicalMemorySize(totalPhysicalMemorySize);
        hostInfo.setFreePhysicalMemorySize(freePhysicalMemorySize);
        hostInfo.setUsedMemory(totalPhysicalMemorySize - freePhysicalMemorySize);
        hostInfo.setTotalThread(totalThread);
        hostInfo.setCpuRatio(systemCpuLoad);
        return hostInfo;
    }

    private static void getDiskInfo(HostInfo hostInfo) {
        File[] disks = File.listRoots();
        long freeSpace = 0;
        long usableSpace = 0;
        long totalSpace = 0;
        for (File file : disks) {
            freeSpace += file.getFreeSpace() / KB;
            usableSpace += file.getUsableSpace() / KB;
            totalSpace += file.getTotalSpace() / KB;
        }
        hostInfo.setFreeSpace(freeSpace);
        hostInfo.setUsableSpace(usableSpace);
        hostInfo.setTotalSpace(totalSpace);
    }

    private static class MemoryPoolInfo {

        private String key;
        private String name;
        private long init;
        private long used;
        private long committed;
        private long max;

        public MemoryPoolInfo() {
        }

        public MemoryPoolInfo(String name, long init, long used, long committed, long max) {
            this.key = name;
            this.name = name;
            this.init = init;
            this.used = used;
            this.committed = committed;
            this.max = max;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getInit() {
            return init;
        }

        public void setInit(long init) {
            this.init = init;
        }

        public long getUsed() {
            return used;
        }

        public void setUsed(long used) {
            this.used = used;
        }

        public long getCommitted() {
            return committed;
        }

        public void setCommitted(long committed) {
            this.committed = committed;
        }

        public long getMax() {
            return max;
        }

        public void setMax(long max) {
            this.max = max;
        }
    }

    static class MxBean {

        private final Map<String, Counter> counters;

        private final RuntimeMXBean runtimeBean;

        private final OperatingSystemMXBean osBean;

        private final MemoryMXBean memoryMXBean;

        private final ThreadMXBean threadBean;

        private final ClassLoadingMXBean classLoadingBean;

        private final List<GarbageCollectorMXBean> gcMxBeans;

        private final List<MemoryPoolMXBean> memoryPoolMXBeans;

        MxBean(Map<String, Counter> counters, RuntimeMXBean runtimeBean, OperatingSystemMXBean osBean,
               MemoryMXBean memoryMXBean, ThreadMXBean threadBean, ClassLoadingMXBean classLoadingBean,
               List<GarbageCollectorMXBean> gcMxBeans, List<MemoryPoolMXBean> memoryPoolMXBeans) {
            this.counters = counters;
            this.runtimeBean = runtimeBean;
            this.osBean = osBean;
            this.memoryMXBean = memoryMXBean;
            this.threadBean = threadBean;
            this.classLoadingBean = classLoadingBean;
            this.gcMxBeans = gcMxBeans;
            this.memoryPoolMXBeans = memoryPoolMXBeans;
        }

        public Map<String, Counter> getCounters() {
            return counters;
        }

        public RuntimeMXBean getRuntimeBean() {
            return runtimeBean;
        }

        public OperatingSystemMXBean getOsBean() {
            return osBean;
        }

        public MemoryMXBean getMemoryMXBean() {
            return memoryMXBean;
        }

        public ThreadMXBean getThreadBean() {
            return threadBean;
        }

        public ClassLoadingMXBean getClassLoadingBean() {
            return classLoadingBean;
        }

        public List<GarbageCollectorMXBean> getGcMxBeans() {
            return gcMxBeans;
        }

        public List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
            return memoryPoolMXBeans;
        }
    }
}