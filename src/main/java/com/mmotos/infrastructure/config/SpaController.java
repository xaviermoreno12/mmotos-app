package com.mmotos.infrastructure.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    /**
     * Catch-all for client-side routes: any path that doesn't contain a dot (file extension)
     * and doesn't start with /api/ is forwarded to index.html so React Router handles it.
     */
    @GetMapping(value = "/{path:^(?!(?:api|assets|favicon))[^\\.]*}/**")
    public String spaDeep() {
        return "forward:/index.html";
    }

    @GetMapping(value = "/{path:^(?!(?:api|assets|favicon))[^\\.]*}")
    public String spaShallow() {
        return "forward:/index.html";
    }

    @GetMapping(value = "/")
    public String root() {
        return "forward:/index.html";
    }
}
