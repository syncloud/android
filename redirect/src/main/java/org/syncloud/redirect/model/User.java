package org.syncloud.redirect.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    public boolean active;
    public String email;
    public List<Domain> domains;
}
