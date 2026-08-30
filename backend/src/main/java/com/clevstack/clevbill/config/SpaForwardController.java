package com.clevstack.clevbill.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serves the SPA shell for client-side routes typed directly into the
 * browser (e.g. a till reloading on {@code /checkout}) or requested by a
 * cold link. The first path segment must be dot-free and not {@code api}
 * or {@code assets}:
 * <ul>
 *   <li>excluding {@code api} keeps this off the REST API
 *   <li>excluding {@code assets} keeps this off Vite's hashed JS/CSS —
 *       the dot check alone doesn't catch these, since the extension is
 *       on the second path segment ({@code /assets/index-XXXX.js}), not
 *       the first
 *   <li>the dot check itself stops single-segment static files
 *       ({@code /index.html}, {@code /favicon.svg}) from matching —
 *       without it, a request for {@code /index.html} would match this
 *       same pattern and forward to itself forever
 * </ul>
 */
@Controller
public class SpaForwardController {

    @RequestMapping(value = "/{path:^(?!api|assets)[^.]*$}/**")
    public String forward() {
        return "forward:/index.html";
    }
}
