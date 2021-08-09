/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.krlvm.powertunnel.utilities;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.impl.ProxyUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Utility for working with HTTP requests and responses
 *
 * @author krlvm
 */
public class HttpUtility {

    /**
     * Retrieves stub with reason in a HTML-body
     *
     * @param reason - reason
     * @return stub packet
     */
    public static HttpResponse getStub(String reason) {
        return getClosingResponse("<html><head>\n"
                + "<title>Access denied</title>\n"
                + "</head><body>\n"
                + "<p style='color: red; font-weight: bold'>" + reason + "</p>"
                + "</body></html>\n");
    }

    /**
     * Retrieves response with connection-close mark
     *
     * @param html - HTML code
     * @return HttpResponse with connection-close mark
     */
    public static HttpResponse getClosingResponse(String html) {
        HttpResponse response = getResponse(html);
        response.headers().set(HttpHeaders.Names.CONNECTION, "close");
        return response;
    }

    /**
     * Retrieves response with HTML code
     *
     * @param html - HTML code
     * @return HttpResponse with HTML code
     */
    public static HttpResponse getResponse(String html) {
        String body = "<!DOCTYPE html>\n" + html;
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ByteBuf content = Unpooled.copiedBuffer(bytes);
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY, content);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
        response.headers().set("Content-Type", "text/html; charset=UTF-8");
        response.headers().set("Date", ProxyUtils.formatDate(new Date()));
        return response;
    }

    /**
     * Retrieves response with HTML code
     *
     * @param html - HTML code
     * @param headers - response headers
     * @return HttpResponse with HTML code
     */
    public static HttpResponse getResponse(String html, int status, Map<String, String> headers) {
        String body = "<!DOCTYPE html>\n" + html;
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ByteBuf content = Unpooled.copiedBuffer(bytes);
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(status), content);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
        response.headers().set("Content-Type", "text/html; charset=UTF-8");
        response.headers().set("Date", ProxyUtils.formatDate(new Date()));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.headers().set(header.getKey(), header.getValue());
        }
        return response;
    }

    /**
     * Retrieves formatted host string
     *
     * @param host - initial host value
     * @return formatted host string
     */
    public static String formatHost(String host) {
        return host.replace(":443", "").replace("www.", "");
    }


    /**
     * Generates upstream proxy auth code
     */
    public static String generateAuthCode(String username, String password) {
        String credential = username + ":" + password;
        byte[] data = credential.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(data).trim();
    }
}
