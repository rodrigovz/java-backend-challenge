package com.altruist.core.snippets;

public abstract class SnippetsPathFactory {
    abstract String getEndpointName();

    String create(String version, String usecase) {
        return getEndpointName() + "/" + version + "/" + usecase;
    }
}