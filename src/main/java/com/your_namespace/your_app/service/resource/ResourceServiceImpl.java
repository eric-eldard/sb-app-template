package com.your_namespace.your_app.service.resource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public final class ResourceServiceImpl implements ResourceService
{
    @Override
    public List<Resource> getFileResources(String pathPattern) throws IOException
    {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(pathPattern);
        return List.of(resources);
    }

    @Override
    public List<Resource> getClasspathResources(String classpathPattern) throws IOException
    {
        ClassLoader loader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
        Resource[] resources = resolver.getResources(classpathPattern) ;
        return List.of(resources);
    }

    @Override
    @SneakyThrows
    public Resource resolveResource(String path)
    {
        Resource resource;
        if (path.startsWith("file:"))
        {
            resource = new FileSystemResource(path.replace("file:", ""));
        }
        else if (path.startsWith("http:") || path.startsWith("https:"))
        {
            resource = new UrlResource(path);
        }
        else if (path.startsWith("classpath:"))
        {
            resource = new ClassPathResource(path.replace("classpath:", ""));
        }
        else
        {
            throw new IllegalArgumentException("Cannot determine resource protocol: [" + path + ']');
        }
        return resource;
    }

    @Override
    @Nullable
    public String resourceUriToString(Resource resource)
    {
        String fileUri = null;
        try
        {
            fileUri = resource.getURI().toString();
        }
        catch (IOException ex)
        {
            log.error("Could not load resource [{}]: {}", resource.getFilename(), ex.getMessage());
        }
        return fileUri;
    }
}