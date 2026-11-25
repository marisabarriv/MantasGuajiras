package com.mantasguajiras.backend.security.service;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HexFormat;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Dirección desde la que llega una petición.
 *
 * Detrás del servidor web (Caddy) la dirección real llega en
 * X-Forwarded-For y Tomcat la aplica (SERVER_FORWARD_HEADERS_STRATEGY en
 * deploy/docker-compose.yml). Sin esa opción la cabecera se ignora: nadie
 * puede inventarse la dirección.
 */
public final class ClientAddress {

    private ClientAddress() {
    }

    public static String of(HttpServletRequest request) {
        return normalize(request.getRemoteAddr());
    }

    /** IPv6: un mismo equipo puede usar todo su /64, así que cuenta como una sola dirección. */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String value = address.split("%")[0];
        if (!value.contains(":") || !value.matches("[0-9a-fA-F:.]+")) {
            return value;
        }
        try {
            InetAddress parsed = InetAddress.getByName(value);
            if (parsed instanceof Inet6Address && !parsed.isLoopbackAddress()) {
                return HexFormat.of().formatHex(Arrays.copyOf(parsed.getAddress(), 8)) + "/64";
            }
            return parsed.getHostAddress();
        } catch (UnknownHostException notAnAddress) {
            return value;
        }
    }
}
