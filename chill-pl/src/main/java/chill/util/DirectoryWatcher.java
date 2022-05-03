package chill.util;
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import chill.utils.ChillLogs;
import chill.utils.Pair;

import java.nio.file.*;

import static chill.utils.TheMissingUtils.safely;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class DirectoryWatcher {

    static ChillLogs.LogCategory LOG = ChillLogs.get(DirectoryWatcher.class);

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private final Consumer<Pair<WatchEvent<Path>, Path>> onChange;

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    public static void watch(Path of, Consumer<Pair<WatchEvent<Path>, Path>> callback) {
        try {
            new DirectoryWatcher(of, callback);
        } catch (Exception e) {
            LOG.error("Unable to watch " + of.toAbsolutePath(), e);
        }
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path prev = keys.get(key);
        if (prev == null) {
            LOG.debug("register: {}", dir);
        } else {
            if (!dir.equals(prev)) {
                LOG.debug("update: {} -> {}", prev, dir);
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) {
        safely(()->{
            Files.walkFileTree(start, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    safely(() -> register(dir));
                    return FileVisitResult.CONTINUE;
                }
            });
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    private DirectoryWatcher(Path dir, Consumer<Pair<WatchEvent<Path>, Path>> onChange) {
        this.watcher = safely(() -> FileSystems.getDefault().newWatchService());
        this.keys = new HashMap();
        this.onChange = onChange;
        registerAll(dir);
        new Thread(this::monitor).start();
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void monitor() {
        LOG.info("Monitoring...");
        while (true) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                LOG.error("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                onChange.accept(Pair.of(ev, child));

                // if directory is created then register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        registerAll(child);
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

}