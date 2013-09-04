package org.opennms.netmgt.model;

import javax.persistence.*;

@Entity
@Table(name="filters")
public class OnmsFilter {

    public static enum Page {
        EVENT, ADMIN;
    }


    @Id
    @SequenceGenerator(name="filterSequence", sequenceName="filternextid")
    @GeneratedValue(generator="filterSequence")
    @Column(name="filterid", nullable=false)
    private Integer id;

    @Column(name="username", nullable=false)
    private String username;

    @Column(name="filtername", nullable=false)
    private String name;

    @Column(nullable=false)
    private String filter;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private Page page;

    public OnmsFilter() { }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
