$(document).ready(function () {
    var keepRunning = false;
    var minute = 1 * 60 * 1000;
    var currentProject;
    var currentModule;
    var currentBranch;
    var currentFile;
    var currentClass;
    var lineMapping = {};
    var linePrefix = "line";
    var decompilerFile = false;
    var downSourceAllow = false;
    var jarDebug = false;
    var base64 = new Base65();
    var currentHost = {};
    var currentAppCode;
    var currentMonitor = {};
    var EMPTY_TIMER_DATA = ["-", "-"];
    var EMPTY_COUNT_DATA = ["-"];
    var _monitor = {
        interval: -1,
        data: [],
        quotas: new Set(),
        time: [],
        start: "",
        end: "",
        curve: {}
    }
    var quotaType = {
        "counter": "counter",
        "timer": "timer"
    }

    function addMonitor() {
        var command = "qmonitoradd " + currentMonitor.uuid + " " + currentMonitor.source + " " + currentMonitor.line;
        elfWS.sendCommand(currentHost, 40, command, stop, handleResult);
    }

    function reportList() {
        var start = new Date($("#startDate").val()).getTime();
        var end = new Date($("#endDate").val()).getTime();
        _monitor.start = start;
        _monitor.end = end;
        var command = "qmonitorquery list '' -s " + start + " -e " + end;
        elfWS.send(currentHost, 41, command, {type: "list", startTime: start, endTime: end}, stop, handleResult);
    }

    function relaodClasses() {
        var command = "jardebug -r " + $("#reload-params").val();
        elfWS.sendCommand(currentHost, 9, command, stop, handleResult);
    }

    function getAllClass() {
        var command = "jardebug";
        elfWS.sendCommand(currentHost, 9, command, stop, handleResult);
    }

    function getClassPath(className) {
        var command = "jarclasspath " + encodeURI(className);
        elfWS.sendCommand(currentHost, 9, command, stop, handleResult);
    }

    function decompileClass(className, classPath) {
        var command = "decompilerclass " + encodeURI(className) + " " + encodeURI(classPath);

        elfWS.send(currentHost, 50, command, {className: encodeURI(className), classPath: encodeURI(classPath)}, stop, handleResult);
    }

    function getReleaseInfo() {
        var command = "qdebugreleaseinfo " + currentHost.logDir;
        elfWS.sendCommand(currentHost, 8, command, stop, handleResult);
    }

    function buildMonitorResult(data) {
        cleanMonitorResult();
        initData(data);
        initXAxis();
        getAllQuotas();
        buildQuotaList();
        initCurve();
        parseData();
        fillData();
    }

    function initData(data) {
        _monitor.data = data.map(function (item) {
            return JSON.parse(item);
        });
    }

    function initXAxis() {
        _monitor.time = [];
        _monitor.data.forEach(function (value) {
            _monitor.time.push(value.timestamp)
        })
        _monitor.interval = -1;
        if (_monitor.time.length >= 2) {
            _monitor.interval = (_monitor.time[1] - _monitor.time[0]) / minute;
        }
    }

    function getAllQuotas() {
        _monitor.data.forEach(function (metrics) {
            metrics.metricsData.forEach(function (metric) {
                _monitor.quotas.add(metric.name);
            })
        })
        _monitor.quotas = new Set(Array.from(_monitor.quotas).sort());
    }

    function parseData() {
        _monitor.data.forEach(function (monitor) {
            var currentQuotas = Array.from(_monitor.quotas);
            monitor.metricsData.forEach(function (metric) {
                _monitor.curve[metric.name].data.push(metric.data);
                currentQuotas.remove(metric.name);
            })
            currentQuotas.forEach(function (quota) {
                if (_monitor.curve[quota].type == quotaType.counter) {
                    _monitor.curve[quota].data.push(EMPTY_COUNT_DATA);
                } else if (_monitor.curve[quota].type == quotaType.timer) {
                    _monitor.curve[quota].data.push(EMPTY_TIMER_DATA);
                }
            })
        })
    }

    function fillData() {
        _monitor.quotas.forEach(function (quota) {
            if (endWith(quota, "_exception")) {
                return;
            }
            var series = [];
            if (_monitor.curve[quota].type == quotaType.timer) {
                series = [{
                    name: "MIN_1(????????????QPS)",
                    type: 'line',
                    animation: false,
                    yAxisIndex: 0,
                    data: _monitor.curve[quota].data.map(function (value) {
                        return formatFloat(value[0]);
                    })
                }, {
                    name: "P98(???98%???)",
                    type: 'line',
                    animation: false,
                    yAxisIndex: 1,
                    data: _monitor.curve[quota].data.map(function (value) {
                        return formatFloat(value[1]);
                    })
                }]
            } else if (_monitor.curve[quota].type == quotaType.counter) {
                var index = quota.indexOf("_counter");
                var exceptionQuota = quota.substring(0, index) + "_exception";
                series = [{
                    name: "COUNT",
                    type: 'line',
                    animation: false,
                    yAxisIndex: 0,
                    data: _monitor.curve[quota].data.map(function (value) {
                        return formatFloat(value[0]);
                    })
                }]
                if (_monitor.quotas.has(exceptionQuota)) {
                    series.push({
                        name: "EXCEPTION",
                        type: "line",
                        animation: false,
                        yAxisIndex: 1,
                        data: _monitor.curve[exceptionQuota].data.map(function (value) {
                            return formatFloat(value[0]);
                        })
                    })
                }

            } else {
                elf.error(quota + " ???????????????????????????");
                return;
            }
            updateCurve(_monitor.curve[quota].curve, _monitor.time, series);
        })
    }

    function buildQuotaList() {
        var quota = $("#quota-list");
        quota.empty();
        _monitor.quotas.forEach(function (item) {
            if (endWith(item, "_exception")) {
                return;
            }
            var quotaHref = $("<a></a>").addClass("list-group-item").attr("href", "#" + item).append(item);
            quota.append(quotaHref);
        })
    }

    function initCurve() {
        var monitor = $("#monitor-curve");
        _monitor.quotas.forEach(function (quota) {
            if (endWith(quota, "_exception")) {
                _monitor.curve[quota] = {
                    data: new Array(),
                    type: getQuotaType(quota)
                };
                return;
            }
            var hr = $("<div></div>").addClass("col-md-12").append($("<hr>"))
            var quota_div = $("<div></div>").addClass("col-md-12").attr("id", quota).css({
                "height": "350px",
            });
            monitor.append(quota_div).append(hr);
            _monitor.curve[quota] = {
                curve: echarts.init(document.getElementById(quota)),
                data: new Array(),
                type: getQuotaType(quota)
            };
            var yAxis = [];
            var legend = {};
            if (_monitor.curve[quota].type == quotaType.timer) {
                yAxis = [{
                    name: "COUNT",
                    type: "value",
                    splitLine: {
                        show: false
                    }
                }, {
                    name: "TIME",
                    type: "value",
                    splitLine: {
                        show: false
                    }
                }];
                legend = {
                    data: [
                        "MIN_1(????????????QPS)",
                        "P98(???98%???)"
                    ],
                    selected: {
                        "MIN_1(????????????QPS)": true,
                        "P98(???98%???)": true
                    },
                    y: "bottom",
                    padding: 10,
                    itemGap: 15
                };
            } else if (_monitor.curve[quota].type == quotaType.counter) {
                yAxis = [{
                    name: "COUNT",
                    type: "value",
                    splitLine: {
                        show: false
                    }
                }, {
                    name: "EXCEPTION",
                    type: "value",
                    splitLine: {
                        show: false
                    }
                }];
                legend = {
                    data: ["COUNT", "EXCEPTION"],
                    y: "bottom",
                    padding: 10,
                    itemGap: 15
                };

            } else {
                elf.error(quota + " ???????????????????????????");
                return;
            }
            drawCurve(_monitor.curve[quota].curve, quota, legend, _monitor.time, yAxis);
        })
    }

    function updateCurve(curve, xAxis, series) {
        curve.setOption({
            xAxis: {
                data: xAxis.map(function (value) {
                    return dateFormat(value);
                })
            },
            series: series
        })
    }

    function drawCurve(curve, title, legend, xAxis, yAxis) {
        if (_monitor.interval != -1) {
            title += "---??????" + _monitor.interval + "??????";
        }
        curve.setOption({
            title: {
                text: title,
                x: 130
            }, tooltip: {
                trigger: 'axis'
            },
            toolbox: {
                show: true,
                feature: {
                    saveAsImage: {show: true}
                },
                x2: 110
            },
            grid: {
                y2: 50,
                x2: 120,
                y: 100,
                x: 130
            },
            legend: legend,
            xAxis: {
                boundaryGap: true,
                data: xAxis.map(function (value) {
                    return dateFormat(value);
                }),
                axisTick: {
                    alignWithLabel: true
                }
            }, dataZoom: [{
                type: 'inside',
                show: false
            }, {
                top: 35,
                left: 130
            }],
            yAxis: yAxis,
            visualMap: {
                top: 10,
                right: 10,
                pieces: [],
                outOfRange: {
                    color: '#999'
                }
            }
        })
    }

    function getQuotaType(name) {
        if (endWith(name, "_timer")) {
            return quotaType.timer;
        } else if (endWith(name, "_counter")) {
            return quotaType.counter;
        } else if (endWith(name, "_exception")) {
            return quotaType.counter;
        }
    }

    function buildFilePanel(file) {
        $("#file-path").val("");
        $("#project").val(currentProject);
        $("#branch").val(currentBranch);
        $("#app").val(currentHost.appCode);
        $("#host").val(currentHost.host + ":" + currentHost.port);
        $(".file-panel").show();
        $("#splitter-handle").show();

        decompilerFile = false;
        buildFileContent(file.fileName, base64.decode(file.content))
    }

    function buildDecompilerFileContent(filename, fileContent) {
        $("#file-path").val("");
        $("#project").val(currentProject);
        $("#branch").val(currentBranch);
        $("#app").val(currentHost.appCode);
        $("#host").val(currentHost.host + ":" + currentHost.port);
        $(".file-panel").show();
        $("#splitter-handle").show();

        decompilerFile = true;
        fileContent = base64.decode(fileContent);
        buildFileContent(filename, buildLineMapping(fileContent));
    }

    function buildMavenSourceFileContent(filename, fileContent) {
        $("#file-path").val("");
        $("#project").val(currentProject);
        $("#branch").val(currentBranch);
        $("#app").val(currentHost.appCode);
        $("#host").val(currentHost.host + ":" + currentHost.port);
        $(".file-panel").show();
        $("#splitter-handle").show();

        decompilerFile = false;
        buildFileContent(filename, fileContent);
    }

    function buildFileContent(filename, fileContent) {

        currentFile = filename;

        $(".path-tab").empty();
        $(".path-tab").append($("<li></li>").append(currentFile).addClass("active"));

        $("#code-line").val("");
        $("#conditional-breakpoint").val("");

        $("#file-content-panel").empty();
        var language = getMode(filename);
        var content = hljs.highlight(language, fileContent);
        var codetext = content.value.split("\n");
        var code = $("<code></code>");
        var lineNumberPanel = $("<div></div>").addClass("line-number");
        var codeLinePanel = $("<div></div>").addClass("code-line");
        codetext.forEach(function (value, index) {
            var lineNumber = $("<div></div>").addClass("number").append($("<span></span>").append(index + 1));
            var codeLine = $("<div></div>").addClass("line").append($("<span></span>").addClass("code-content").append(value));
            lineNumber.click(function () {
                if (decompilerFile) {
                    var key = linePrefix + (index + 1);
                    var line = lineMapping[key];
                    if (line != null && line != undefined && line.length > 0) {
                        $("#code-line").val(Number(line[0]));
                    } else {
                        $("#code-line").val("");
                    }
                } else {
                    $("#code-line").val(index + 1);
                }
                $("#file-path").val(currentFile + ":" + (index + 1));
                $(".number").removeClass("selected");
                $(this).addClass("selected")
                $(".line").removeClass("selected")
                codeLine.addClass("selected");
            })
            lineNumber.mouseover(function () {
                $(this).addClass("mouse-over")
                codeLine.addClass("mouse-over");
            })
            lineNumber.mouseout(function () {
                $(".number").removeClass("mouse-over");
                $(".line").removeClass("mouse-over")
            })
            lineNumberPanel.append(lineNumber);
            code.append(codeLine);
        })
        var pre = $("<pre></pre>").append(code)
        codeLinePanel.append(pre);
        $("#file-content-panel").append(lineNumberPanel).append(codeLinePanel);
    }


    function buildLineMapping(fileContent) {
        lineMapping = {};
        var index = fileContent.lastIndexOf("Lines mapping:");
        if (index >= 0) {
            var map = fileContent.substring(index + 15);
            var mapping = map.split("\n");
            mapping.forEach(function (line) {
                if (line != null && line != undefined && line != "") {
                    var lineNumbers = line.split(" <-> ");
                    if (lineNumbers.length == 2) {
                        var key = linePrefix + lineNumbers[1].trim();
                        if (lineMapping[key] == null || lineMapping[key] == undefined) {
                            lineMapping[key] = new Array();
                        }
                        lineMapping[key].push(lineNumbers[0]);
                    }
                }
            })
            fileContent = fileContent.substring(0, index);
        }
        return fileContent;
    }

    function buildJarDebugPanel(result) {
        var data = result.map(function (item) {
            return {
                "name": item
            }
        });
        $('#jar-debug-table').bootstrapTable('removeAll');
        $('#jar-debug-table').bootstrapTable('append', data);
    }




    function getAppList() {
        currentAppCode = "";
        var select = $(".host-list");
        select.empty();
        select.trigger("chosen:updated");
        $.ajax({
            "url": "/getApps.do",
            "type": "get",
            success: function (ret) {
                if (ret.status === 0) {
                    var select = $(".app-list");
                    select.empty();
                    select.trigger("chosen:updated");
                    select.append($("<option value=''></option>"));
                    ret.data.forEach(function (app) {
                        select.append($("<option></option>").attr("value", app).append(app));
                    })
                    select.chosen({"width": "100%", search_contains: true});
                    select.on('change', function (e) {
                        var appCode = $("#" + e.target.id).val();
                        $("#app-list").val(appCode);
                        $("#monitor-app-list").val(appCode);
                        select.trigger("chosen:updated");
                        if (currentAppCode != appCode) {
                            currentAppCode = appCode;
                            getHosts(appCode);
                        }
                    })
                    select.trigger("chosen:updated");
                    $("#app-host-panel").show();
                } else {
                    elf.error('???????????????????????????' + ret.message);
                }
            }

        });
    }

    function getHosts(app) {
        var select = $(".host-list");
        select.empty();
        $.ajax({
            "url": "/getHosts.do",
            "type": "get",
            "data": {
                "appCode": app
            },
            success: function (ret) {
                if (ret.status == 0) {
                    select.append($("<option value=''></option>"))
                    ret.data.forEach(function (machine) {
                        select.append($("<option></option>").attr("value", JSON.stringify(machine)).append(machine.host + ":" + machine.port));
                    })
                    select.on('change', function (e) {
                        var hostInfo = $("#" + e.target.id).val();
                        currentHost = JSON.parse(hostInfo);
                        $("#host-list").val(hostInfo);
                        $("#monitor-host-list").val(hostInfo);
                        select.trigger("chosen:updated");
                    })
                    select.chosen({"width": "100%", search_contains: true});
                    select.trigger("chosen:updated");
                } else {
                    elf.error('????????????????????????, ???????????????' + ret.message);
                }
            }
        });

    }



    function stop() {
        keepRunning = false;
    }

    function handleResult(content) {
        if (!content) {
            return;
        }
        var result = JSON.parse(content);
        if (!result) {
            return;
        }
        var resType = result.type;
        if (resType == "qmonitorquery") {
            var res = result.data;
            if (res.status == 0) {
                if (res.data.length == 0) {
                    elf.warning("?????????????????????????????????????????????????????????????????????");
                    keepRunning = false;
                } else {
                    buildMonitorResult(res.data);
                }
            } else {
                elf.error(res.message);
                keepRunning = false;
                console.log(res.message);
            }
        } else if (resType == "qmonitoradd") {
            var res = result.data;
            if (res.code == 0) {
                elf.success("??????????????????")
            } else {
                elf.error("?????????????????? " + res.message)
                console.log(res.message);
            }
        } else if (resType == "qdebugreleaseinfo") {
            var res = result.data;
            if (res.code == 0) {
                parseCmInfo(res.data);
            } else {
                console.log(res.message);
                elf.warning(res.message + "????????????????????????????????????")
            }
            getAllClass();
        } else if (resType == "jardebug") {
            var res = result.data;
            if (res.code == 0) {
                $("#app-host-panel").hide();
                $("#jar-debug-panel").show();
                jarDebug = true;
                buildJarDebugPanel(res.data)
            } else {
                elf.error(res.message);
                console.log(res.message);
            }
        } else if (resType == "jarclasspath") {
            var res = result.data;
            console.log(res);
            if (res.code == 0) {
                if (res.data && res.data.maven) {
                    getFileFromMaven(res.id, res.data);
                } else {
                    decompileClass(res.id, res.data.classPath);
                }
            } else {
                elf.error(res.message);
                console.log(res.message);
            }
        } else if (resType == "decompilerclass") {
            var res = result.data;
            if (res.code == 0) {
                buildDecompilerFileContent(res.id, res.data);
            } else {
                elf.error(res.message)
                console.log(res.message)
            }
        }
    }

    function parseCmInfo(content) {
        $.ajax({
            url: 'api/release/info/parse.do',
            method: 'POST',
            dataType: 'JSON',
            data: {
                content: content
            },
            success: function (res) {
                if (res.status == 0) {
                    var relaeaseInfo = res.data;
                    console.log(relaeaseInfo);
                    currentProject = relaeaseInfo.project;
                    currentModule = relaeaseInfo.module;
                    currentBranch = relaeaseInfo.output;
                } else {
                    elf.warning("release info ???????????????" + res.message + "????????????????????????????????????")
                }
            },
            error: function (error) {
                elf.warning("release info ???????????????" + res.message + "????????????????????????????????????")
            }
        })
    }

    $("#app-host-choose").click(function () {
        if (!currentAppCode) {
            elf.warning("??????????????????")
            return;
        }
        if (!currentHost) {
            elf.warning("??????????????????")
            return;
        }
        getAllClass();
    });

    /*$("#back-app-host-panel").click(function () {
        $("#file-tree-panel").hide();
        $("#monitor-result-panel").hide();
        $("#app-host-panel").show();
    })*/

    $(".back-app-host-panel").click(function () {
        $("#jar-debug-panel").hide();
        $("#monitor-result-panel").hide();
        $("#app-host-panel").show();
    })

    $("#back-class-table").click(function () {
        currentFile = "";
        decompilerFile = false;
        $(".file-panel").hide();
        $("#splitter-handle").hide();
        $("#jar-debug-panel").show();
    })

    $("#classes-reload").click(function () {
        $('#jar-debug-table').bootstrapTable('removeAll');
        relaodClasses();
    })

    $("#add-monitor").click(function () {
        if ($("#add-monitor").attr("disabled") == "disabled") {
            return;
        }
        if (currentFile.lastIndexOf(".java") != currentFile.length - 5) {
            elf.error("?????? Debug ????????? Java ??????");
            return;
        }

        var line = $("#code-line").val();
        if (line == null || line == undefined || line == "" || line < 0) {
            if (decompilerFile) {
                elf.error("??????????????????????????????????????????????????????");
            } else {
                elf.error("???????????????????????????????????????")
            }
            return;
        } else {
            currentMonitor = {
                line: line,
                app: currentHost.appCode,
                host: currentHost.ip,
                source: currentClass.replace(/\./g, "/") + ".java",
                uuid: uuid()
            }
            addMonitor();
        }
    })

    $("#search-monitor").click(function () {
        reportList();
    })

    $("#refresh-monitor").click(function () {
        var end = dateFormat(new Date().getTime());
        var start = $("#startDate").val();
        $('#reportrange span').html(start + "-" + end);
        $('#endDate').val(end);
        reportList();
    })

    $("#go-add-monitor").click(function () {
        $("#monitor-result-panel").hide();
        $("#app-host-panel").show();
        $("#jar-debug-panel").hide();
        $(".file-panel").hide();
        $("#splitter-handle").hide();
    })
    $("#go-monitor-result").click(function () {
        if (!currentAppCode) {
            elf.warning("??????????????????")
            return;
        }
        if (!currentHost) {
            elf.warning("??????????????????")
            return;
        }
        showMonitorResult()
        reportList();
    })
    $("#watch-monitor-result").click(function () {
        showMonitorResult()
        reportList();
    })

    var isResizing = false;
    var lastDownX = 0;
    var flag = 0;
    var width = document.body.clientWidth;
    var isClose = false;
    var tooLeft = false;
    $("#splitter-handle").on("mousedown", function (e) {
        flag = 0;
        isResizing = true;
        lastDownX = e.clientX;
    })
    $(window).resize(function () {
        width = document.body.clientWidth;
    })

    $(document).on("mousemove", function (e) {
        flag = 1;
        if (!isResizing) {
            return;
        }
        var x = e.clientX;
        if (x < width * 0.4) {
            if (!tooLeft) {
                elf.warning("?????????????????????");
                tooLeft = true;
            }
            return;
        } else {
            tooLeft = false;
        }
        if (width - x < 300 && lastDownX < x) {
            closeDebugResultPanel();
        } else {
            isClose = false;
            $("#debug-result-panel").show();
            var codeWidth = x / width * 100 + "%";
            $("#code-panel").css("width", codeWidth);
            $("#file-content-panel").css("width", codeWidth);
            $("#splitter-handle").css("margin-left", codeWidth).attr("title", "???????????????");
            $("#debug-result-panel").css("margin-left", codeWidth).css("width", (1 - x / width) * 100 + "%");
        }
        lastDownX = x;
    })
    $(document).on('mouseup', function (e) {
        isResizing = false;
        if (flag == 0) {
            if (!isClose) {
                closeDebugResultPanel();
            } else {
                openDebugResultPanel();
            }
        }
        var w = $("#debug-result-panel").css("width").replace("px", "");
        if (w < 300) {
            closeDebugResultPanel();
        }
    })

    function closeDebugResultPanel() {
        isClose = true;
        var x = width;
        $("#debug-result-panel").hide();
        var codeWidth = x / width * 100 + "%";
        $("#code-panel").css("width", codeWidth);
        $("#file-content-panel").css("width", codeWidth);
        $("#splitter-handle").css("margin-left", codeWidth).attr("title", "???????????????");
    }

    function openDebugResultPanel() {
        isClose = false;
        var x = width * 0.6;
        $("#debug-result-panel").show();
        var codeWidth = x / width * 100 + "%";
        $("#code-panel").css("width", codeWidth);
        $("#file-content-panel").css("width", codeWidth);
        $("#splitter-handle").css("margin-left", codeWidth).attr("title", "???????????????");
        $("#debug-result-panel").css("margin-left", codeWidth).css("width", (1 - x / width) * 100 + "%");
    }

    function showMonitorResult() {
        cleanMonitorResult();
        //???????????????????????????
        var currentTime = new Date().getTime();
        var startDate = dateFormat(new Date(currentTime - 60 * 60 * 1000));
        var endDate = dateFormat(currentTime);
        $('#reportrange span').html(startDate + "-" + endDate);
        $('#startDate').val(startDate);
        $('#endDate').val(endDate);

        $("#monitor-app").val(currentHost.appCode);
        $("#monitor-host").val(currentHost.host + ":" + currentHost.port);

        $("#monitor-result-panel").show();
        $("#app-host-panel").hide();
        $("#file-tree-panel").hide();
        $(".file-panel").hide();
        $("#splitter-handle").hide();
    }


    function cleanMonitorResult() {
        $("#monitor-curve").empty();
        $("#quota-list").empty();
    }

    function init() {
        getAppList();
        initJarDebugTable();
    };

    function initJarDebugTable() {
        $('#jar-debug-table').bootstrapTable({
            data: [{}],
            striped: true, //????????????????????????
            pageNumber: 1, //????????????????????????
            pagination: true,//????????????
            sidePagination: 'client',//server:??????????????????|client???????????????
            pageSize: 10,//???????????????
            pageList: [10, 20, 50, 100],//????????????????????????
            showRefresh: true,//????????????
            search: true,
            toolbar: "#toolbar",
            searchAlign: "left",
            buttonsAlign: "left",
            columns: [{
                title: 'Class',
                field: 'name',
                sortable: true,
                searchable: true
            }, {
                title: "??????",
                field: "operate",
                events: operateEvents,
                formatter: function () {
                    return "<a class='jar-class-monitor'>??????</a>"
                }
            }],
            onRefresh: function () {
                $('#jar-debug-table').bootstrapTable('removeAll');
                getAllClass();
            }
        });
    }

    window.operateEvents = {
        "click .jar-class-monitor": function (e, value, row, index) {
            currentClass = row.name;
            //????????????????????????????????????
            downSourceAllow = false;
            $("#down-source").hide();
            $("#down-source").unbind("click");

            if (currentProject && currentBranch) {
                getFile(currentProject, currentBranch, currentModule, currentClass, function () {
                    getClassPath(currentClass)
                })
            } else {
                getClassPath(currentClass);
            }

        }
    }

    function dateFormat(dateStr) {
        var date = new Date(dateStr)
        var year = date.getFullYear();
        var month = ("0" + (date.getMonth() + 1)).slice(-2);
        var day = ("0" + date.getDate()).slice(-2);
        var h = ("0" + date.getHours()).slice(-2);
        var m = ("0" + date.getMinutes()).slice(-2);
        return year + "-" + month + "-" + day + " " + h + ":" + m;
    }

    function uuid() {
        return UUID.prototype.createUUID()
    }

    Array.prototype.remove = function (val) {
        var index = this.lastIndexOf(val);
        if (index > -1) {
            this.splice(index, 1);
        }
    };

    function formatFloat(value) {
        return Math.round(value * 100000000) / 100000000;
    }

    var EXT_MODE_MAPPINGS = {
        "m": "objectivec",
        "mm": "objectivec",
        "sql": "sql",
        "xml": "xml",
        "md": "markdown",
        "perl ": "perl",
        "pl ": "perl",
        "java": "java",
        "csh": "bash",
        "ksh": "bash",
        "sh": "shell",
        "json": "json",
        "ini": "ini",
        "css": "css",
        "ts": "javascript",
        "js": "javascript",
        "rb": "ruby",
        "cs": "cs",
        "coffee": "coffeescript",
        "go": "go",
        "php": "php",
        "cpp": "cpp",
        "properties": "properties",
        "diff": "diff",
        "py": "python",
        "c": "c",
        "html": "xml",
        "groovy": "groovy",
        "gradle": "gradle",
        "swift": "swift"
    };

    function getMode(filename) {
        if (filename) {
            var dotIndex = filename.lastIndexOf('.');
            if (dotIndex == -1) {
                return 'java';
            } else {
                var ext = filename.substr(dotIndex + 1);
                if (EXT_MODE_MAPPINGS[ext]) {
                    return EXT_MODE_MAPPINGS[ext];
                } else {
                    return 'java';
                }
            }
        }
        return 'java';
    }

    function endWith(value, suffix) {
        return startWith(value, suffix, value.length - suffix.length);
    }

    function startWith(value, prefix, toffset) {
        if (!toffset) {
            toffset = 0;
        }
        var ta = value;
        var to = toffset;
        var pa = prefix;
        var po = 0;
        var pc = prefix.length;
        if ((toffset < 0) || (toffset > value.length - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (ta.charAt(to++) != pa.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

    var datePickerOptions = {
        // startDate: moment().startOf('day'),
        // endDate: moment(),
        minDate: moment().subtract('days', 3), //????????????
        maxDate: moment(), //????????????
        dateLimit: {days: 30}, //???????????????????????????
        showDropdowns: true,
        showWeekNumbers: false, //?????????????????????
        timePicker: true, //???????????????????????????
        timePickerIncrement: 1, //?????????????????????????????????
        timePicker12Hour: false, //????????????12????????????????????????
        ranges: {
            '??????1??????': [moment().subtract('hours', 1), moment()],
            '??????2??????': [moment().subtract('hours', 2), moment()],
            '??????': [moment().startOf('day'), moment()],
            '??????': [moment().subtract('days', 1).startOf('day'), moment().subtract('days', 1).endOf('day')],
            '??????3???': [moment().subtract('days', 3), moment()]
        },
        opens: 'right', //??????????????????????????????
        buttonClasses: ['btn btn-default'],
        applyClass: 'btn-small btn-primary blue',
        cancelClass: 'btn-small',
        format: 'YYYY-MM-DD HH:mm', //?????????from???to ?????????????????????
        separator: ' to ',
        locale: {
            applyLabel: '??????',
            cancelLabel: '??????',
            fromLabel: '????????????',
            toLabel: '????????????',
            customRangeLabel: '?????????',
            daysOfWeek: ['???', '???', '???', '???', '???', '???', '???'],
            monthNames: ['??????', '??????', '??????', '??????', '??????', '??????',
                '??????', '??????', '??????', '??????', '?????????', '?????????'],
            firstDay: 1
        }
    }

    $('#reportrange-message').daterangepicker(datePickerOptions, function (start, end, label) {//????????????????????????
        $('#reportrange-message span').html(start.format('YYYY-MM-DD HH:mm') + ' - ' + end.format('YYYY-MM-DD HH:mm'));
        $('#startDate-message').val(start.format('YYYY-MM-DD HH:mm'));
        $('#endDate-message').val(end.format('YYYY-MM-DD HH:mm'));
    })

    //????????????
    $('#reportrange').daterangepicker(datePickerOptions, function (start, end, label) {//????????????????????????
        $('#reportrange span').html(start.format('YYYY-MM-DD HH:mm') + ' - ' + end.format('YYYY-MM-DD HH:mm'));
        $('#startDate').val(start.format('YYYY-MM-DD HH:mm'));
        $('#endDate').val(end.format('YYYY-MM-DD HH:mm'));
    });

    init();
})