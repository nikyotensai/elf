package com.github.nikyotensai.elf.server.proxy.util.profiler;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.nikyotensai.elf.common.JacksonSerializer;
import com.github.nikyotensai.elf.common.TypeResponse;
import com.github.nikyotensai.elf.remoting.protocol.Datagram;

import io.netty.buffer.ByteBuf;

/**
 * @author cai.wen created on 2019/11/6 8:50
 */
public class ProfilerDatagramHelper {

    private static boolean isProfilerResult(Datagram datagram) {
        return datagram.getHeader().getCode() == -2;
    }

    private static TypeResponse<Map<String, String>> getProfilerResponse(ByteBuf body) {
        byte[] data = new byte[body.readableBytes()];
        body.readBytes(data);
        return JacksonSerializer.deSerialize(data, new TypeReference<TypeResponse<Map<String, String>>>() {
        });
    }

    public static Optional<TypeResponse<Map<String, String>>> getProfilerResponse(Datagram datagram) {
        if (!isProfilerResult(datagram)) {
            return Optional.empty();
        }

        TypeResponse<Map<String, String>> response = getProfilerResponse(datagram.getBody().slice());
        return Optional.of(response);
    }

    public static boolean getResultState(TypeResponse<Map<String, String>> response) {
        Map<String, String> data = response.getData().getData();
        String state = data.get("state");
        return Boolean.parseBoolean(state);
    }

    public static String getProfilerId(TypeResponse<Map<String, String>> response) {
        return response.getData().getData().get("profilerId");
    }
}
