package chill.script.runtime;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.resolve.ResolveOptions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class IvyClassLoader extends ClassLoader {
    Ivy ivy = Ivy.newInstance();
    final ResolveOptions resolveOptions = new ResolveOptions();
    final List<URL> urls = new ArrayList<>();
    private URLClassLoader classLoader;

    public IvyClassLoader() {
        try {
            ivy.configureDefault();
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public IvyClassLoader() {
//        ivy = Ivy.newInstance();
//        try {
//            ivy.configureDefault();
//        } catch (ParseException | IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        ivy.getResolutionCacheManager().clean();
//
//        try {
//            ModuleRevisionId mrid = ModuleRevisionId.newInstance("com.google.code.gson", "gson", "2.10.1");
//            var resolution = ivy.resolve(mrid, new ResolveOptions(), false);
//            var reports = resolution.getAllArtifactsReports();
//            System.out.println("got " + reports.length + " reports");
//            for (var report : reports) {
//                System.out.println(report);
//                var artifact = report.getArtifact();
//                if (!artifact.getType().equals("jar")) {
//                    continue;
//                }
//                System.out.println("artifact: " + artifact + " ; file = " + report.getLocalFile());
//
//            }
//
//        } catch (ParseException | IOException e) {
//            throw new RuntimeException(e);
//        } finally {}
//    }

    public void resolve(ModuleRevisionId mrid) {
        try {
            var report = ivy.resolve(mrid, resolveOptions, false);
            ArtifactDownloadReport artifact = null;
            for (var artifactReport : report.getArtifactsReports(mrid)) {
                if (artifactReport.getType().equals("jar") || artifactReport.getType().equals("bundle")) {
                    artifact = artifactReport;
                }
            }
            if (artifact == null) {
                throw new RuntimeException("not jar targets found for: " + mrid.getOrganisation() + ":" + mrid.getName());
            }

            var file = artifact.getLocalFile();
            urls.add(file.toURI().toURL());
        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        if (classLoader == null) {
            classLoader = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
        }
    }

    @Override
    public Class<?> loadClass(String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
