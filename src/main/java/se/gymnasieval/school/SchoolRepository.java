package se.gymnasieval.school;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data skapar implementationen automatiskt (som Doctrines EntityRepository). */
public interface SchoolRepository extends JpaRepository<School, String> {
}
