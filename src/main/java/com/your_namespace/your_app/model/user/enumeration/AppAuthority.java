package com.your_namespace.your_app.model.user.enumeration;

public enum AppAuthority
{
    AN_AUTHORITY("An authority for you to replace"); // TODO - set your app's custom authorities, or remove

    final String pretty;

    AppAuthority(String pretty)
    {
        this.pretty = pretty;
    }

    public String pretty()
    {
        return pretty;
    }
}