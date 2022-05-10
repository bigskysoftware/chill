package chill.script.templates;

import chill.script.parser.ChillScriptParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static chill.utils.TheMissingUtils.safely;

public class ChillTemplates {

    private static List<String> DEFAULT_SEARCH_PATH = new LinkedList<>(List.of("file:src/main/resources/view",
            "file:src/main/resources/templates",
            "resource:view",
            "resource:templates"));

    public ChillTemplates() {
        path = new LinkedList<>(DEFAULT_SEARCH_PATH);
    }

    private static List<String> path;
    private static Map<String, ChillTemplate> cache;

    public ChillTemplates addToPath(String component) {
        path.add(component);
        return this;
    }

    public ChillTemplates withPath(List<String> path) {
        this.path = new LinkedList<>(path);
        return this;
    }

    public ChillTemplates withCaching() {
        cache = new ConcurrentHashMap<>();
        return this;
    }

    public ChillTemplate get(String fullName) {
        if (cache != null) {
            return cache.computeIfAbsent(fullName, this::getNoCache);
        } else {
            return getNoCache(fullName);
        }
    }

    public ChillTemplate getNoCache(String fullName) {
        String[] nameAndFragment = fullName.split("#"); // ignore fragment
        String name = nameAndFragment[0];
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        TemplateLoader loader = loadSource(name);
        String source = safely(loader::getSource);
        ChillTemplateParser parser = new ChillTemplateParser();
        ChillTemplate template = null;
        try {
            template = parser.parseTemplate(source);
        } catch (ChillScriptParseException e) {
            e.setSource(loader.getFullPath());
            throw e;
        }
        template.setTemplateEngine(this);
        template.setSource(loader);
        return template;
    }

    private TemplateLoader loadSource(String name) {
        List<TemplateLoader> loaders = new LinkedList<>();
        for (String root : path) {
            TemplateLoader loader = TemplateLoader.create(root, name);
            if (loader.resolves()) {
                return loader;
            } else {
                loaders.add(loader);
            }
        }
        throw new TemplateNotFoundException(loaders, name);
    }

    public static abstract class TemplateLoader {
        protected final String root;
        protected final String fileName;
        static TemplateLoader create(String pathElement, String file) {
            String[] typeAndPath = pathElement.split(":");
            String type = typeAndPath[0];
            String root = typeAndPath[1];
            if (Objects.equals(type, "file")) {
                return new FileTemplateLoader(root, file);
            } else {
                return new ResourceTemplateLoader(root, file);
            }
        }

        public TemplateLoader(String root, String fileName) {
            this.root = root;
            this.fileName = fileName;
        }

        public abstract boolean resolves();

        public abstract String getFullPath();

        public abstract String getSource() throws IOException;

        private static class FileTemplateLoader extends TemplateLoader {
            public FileTemplateLoader(String root, String pathString) {
                super(root, pathString);
            }

            @Override
            public boolean resolves() {
                Path path = Path.of(root, fileName);
                return path.toFile().exists();
            }

            @Override
            public String getFullPath() {
                Path path = Path.of(root, fileName);
                return path.toAbsolutePath().toString();
            }

            @Override
            public String getSource() throws IOException {
                Path path = Path.of(root, fileName);
                byte[] bytes = Files.readAllBytes(path);
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }

    private static class ResourceTemplateLoader extends TemplateLoader {
        public ResourceTemplateLoader(String root, String pathString) {
            super(root, pathString);
        }

        @Override
        public boolean resolves() {
            return getClass().getResource("/" + root + fileName) != null;
        }

        @Override
        public String getFullPath() {
            return "resource: " + "/" + root +  fileName;
        }

        @Override
        public String getSource() throws IOException {
            try (InputStream inputStream = getClass().getResourceAsStream("/" + root + fileName)) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                return result.toString(StandardCharsets.UTF_8);
            }
        }
    }

    private static  class TemplateNotFoundException extends RuntimeException {
        public TemplateNotFoundException(List<TemplateLoader> loaders, String name) {
            super(generateTemplateNotFoundMessage(loaders, name));
        }

    }

    private static String generateTemplateNotFoundMessage(List<TemplateLoader> loaders, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("Unable to find a template named : ").append(name).append("\n");
        sb.append("Looked In These Places: \n");
        for (TemplateLoader loader : loaders) {
            sb.append("    - ").append(loader.getFullPath()).append("\n");
        }
        return sb.append("\n").toString();
    }
}
