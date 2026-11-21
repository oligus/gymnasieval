package se.gymnasieval.school;

import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Kopplar schema.graphqls till Java. Metodnamnen matchar fälten i schemat:
 * {@code @QueryMapping schools} ↔ {@code Query.schools}. Fält som {@code name} behöver
 * ingen kod, de läses direkt via getters.
 */
@Controller
class SchoolGraphQlController {

    private final SchoolRepository schools;
    private final SchoolProgramRepository schoolPrograms;
    private final ProgramRepository programs;
    private final MunicipalityRepository municipalities;

    SchoolGraphQlController(SchoolRepository schools, SchoolProgramRepository schoolPrograms,
                            ProgramRepository programs, MunicipalityRepository municipalities) {
        this.schools = schools;
        this.schoolPrograms = schoolPrograms;
        this.programs = programs;
        this.municipalities = municipalities;
    }

    @QueryMapping
    List<School> schools(@Argument String program, @Argument String municipality) {
        return schools.search(program, municipality);
    }

    @QueryMapping
    School school(@Argument String code) {
        return schools.findWithMunicipalityBySchoolUnitCode(code).orElse(null);
    }

    @QueryMapping
    List<Program> programs() {
        return programs.findAll(Sort.by("name"));
    }

    @QueryMapping
    List<Municipality> municipalities() {
        return municipalities.findAll(Sort.by("name"));
    }

    /** Schemat kallar fältet "code", entiteten "schoolUnitCode". */
    @SchemaMapping(typeName = "School", field = "code")
    String schoolCode(School school) {
        return school.getSchoolUnitCode();
    }

    /**
     * School.programs för alla skolor i svaret på en gång: en SQL-fråga i stället för en per skola.
     * Spring samlar ihop skolorna och anropar metoden en gång (via graphql-javas DataLoader).
     */
    @BatchMapping(typeName = "School", field = "programs")
    Map<School, List<SchoolProgram>> programs(List<School> schoolList) {
        Map<String, School> byCode = schoolList.stream()
                .collect(Collectors.toMap(School::getSchoolUnitCode, Function.identity(), (a, b) -> a));
        Map<String, List<SchoolProgram>> programsByCode = schoolPrograms
                .findWithDetailsBySchoolUnitCodes(byCode.keySet()).stream()
                .collect(Collectors.groupingBy(sp -> sp.getId().schoolUnitCode()));

        Map<School, List<SchoolProgram>> result = new LinkedHashMap<>();
        for (School school : schoolList) {
            result.put(school, programsByCode.getOrDefault(school.getSchoolUnitCode(), List.of()));
        }
        return result;
    }
}
