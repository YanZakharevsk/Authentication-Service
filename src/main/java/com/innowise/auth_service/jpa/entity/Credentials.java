package com.innowise.auth_service.jpa.entity;

import com.innowise.auth_service.jpa.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "credentials")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Override
    public final boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ?((HibernateProxy) o).getHibernateLazyInitializer()
                 .getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ?((HibernateProxy) this).getHibernateLazyInitializer()
                 .getPersistentClass()
                : this.getClass();
        if(thisEffectiveClass != oEffectiveClass) return false;
        Credentials credentials = (Credentials) o;
        return getId() != null && Objects.equals(getId(), credentials.getId());
    }

    @Override
    public int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer()
                  .getPersistentClass()
                  .hashCode()
                : getClass().hashCode();
    }

}
