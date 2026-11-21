package se.gymnasieval.school;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Objects;

/** Ett nationellt gymnasieprogram, t.ex. "TE25" = Teknikprogrammet. */
@Entity
public class Program {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    protected Program() {
    }

    public Program(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Program other && Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
