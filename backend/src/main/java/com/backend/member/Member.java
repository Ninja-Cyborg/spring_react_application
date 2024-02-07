package com.backend.member;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "member_email_uniq",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "profile_image_id_uniq",
                        columnNames = "profileImageId"
                )
        })
public class Member implements UserDetails {
    @Id
    @SequenceGenerator(name = "member_id_seq",
                        sequenceName = "member_id_seq",
                        allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "member_id_seq")
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private Integer age;
    // default value for column Gender is defined in sql script
    @Column
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(nullable = false)
    private String password;
    @Column(unique = true)
    private String profileImageId;

    public Member(){}

    public Member(String name, String email, String password, int age, Gender gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
    }

    public Member(Integer id,
                  String name,
                  String email,
                  String password,
                  Integer age,
                  Gender gender,
                  String profileImageId) {
        this(id, name, email, password, age, gender);
        this.profileImageId = profileImageId;
    }

    public Member(Integer id,
                  String name,
                  String email,
                  String password,
                  Integer age,
                  Gender gender) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setProfileImage(String profileImageId){
        this.profileImageId = profileImageId;
    }

    public String getProfileImageId(){
        return profileImageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id) && Objects.equals(name, member.name) && Objects.equals(email, member.email) && Objects.equals(age, member.age) && gender == member.gender && Objects.equals(password, member.password) && Objects.equals(profileImageId, member.profileImageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, age, gender, password, profileImageId);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", password='" + password + '\'' +
                ", profileImageId='" + profileImageId + '\'' +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
