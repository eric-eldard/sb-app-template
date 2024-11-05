package com.your_namespace.your_app.service.resource;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;

/**
 * Utilities for working with file and classpath resources
 */
public interface ResourceService
{
    /**
     * Get a list of resources from the file system using the provided pattern. Supports wildcards * and **.
     */
    List<Resource> getFileResources(String pathPattern) throws IOException;

    /**
     * Get a list of resources from the classpath using the provided pattern. Supports wildcards * and **.
     */
    List<Resource> getClasspathResources(String classpathPattern) throws IOException;

    /**
     * Get a file system, web, or classpath resource, based on the path's prefix.
     */
    Resource resolveResource(String path);

    /**
     * Swallow and log checked exceptions coming out of {@link Resource#getURI()}
     */
    @Nullable
    String resourceUriToString(Resource resource);
}