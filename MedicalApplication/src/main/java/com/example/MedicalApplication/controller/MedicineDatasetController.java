package com.example.MedicalApplication.controller;

import com.example.MedicalApplication.dto.MedicineSuggestionDto;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medicines")
public class MedicineDatasetController {

    private volatile List<String> namesCache = null;
    private volatile List<MedicineSuggestionDto> detailsCache = null;

    @GetMapping(value = "/suggest", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> suggest(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        String query = (q == null) ? "" : q.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return List.of();

        List<String> names = getNames();

        int safeLimit = Math.max(1, Math.min(limit, 200));

        return names.stream()
                .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(query))
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    private List<String> getNames() {
        if (namesCache != null) return namesCache;

        synchronized (this) {
            if (namesCache != null) return namesCache;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(
                            getClass().getClassLoader().getResourceAsStream("data/medicine_dataset.csv"),
                            "Nie znaleziono pliku: resources/data/medicine_dataset.csv"
                    ),
                    StandardCharsets.UTF_8
            ))) {
                CSVParser parser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build()
                        .parse(br);

                Set<String> set = new LinkedHashSet<>();
                for (CSVRecord r : parser) {
                    String name = r.get("name");
                    if (name != null) {
                        String trimmed = name.trim();
                        if (!trimmed.isBlank()) set.add(trimmed);
                    }
                }

                List<String> list = new ArrayList<>(set);
                list.sort(String.CASE_INSENSITIVE_ORDER);

                namesCache = list;
                return namesCache;

            } catch (Exception e) {
                namesCache = List.of();
                return namesCache;
            }
        }
    }

    @GetMapping(value = "/suggest-details", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<MedicineSuggestionDto> suggestDetails(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        String query = (q == null) ? "" : q.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return List.of();

        int safeLimit = Math.max(1, Math.min(limit, 200));

        return getDetails().stream()
                .filter(d -> d.name != null && d.name.toLowerCase(Locale.ROOT).startsWith(query))
                .limit(safeLimit)
                .collect(Collectors.toList());
    }

    private List<MedicineSuggestionDto> getDetails() {
        if (detailsCache != null) return detailsCache;

        synchronized (this) {
            if (detailsCache != null) return detailsCache;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(
                            getClass().getClassLoader().getResourceAsStream("data/medicine_dataset.csv"),
                            "Nie znaleziono pliku: resources/data/medicine_dataset.csv"
                    ),
                    StandardCharsets.UTF_8
            ))) {
                CSVParser parser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build()
                        .parse(br);

                Map<String, MedicineSuggestionDto> map = new LinkedHashMap<>();

                for (CSVRecord r : parser) {
                    String name = safeGet(r, "name");
                    if (name == null) continue;

                    String trimmed = name.trim();
                    if (trimmed.isBlank()) continue;

                    String uses = buildUsesSummary(r);

                    map.putIfAbsent(trimmed, new MedicineSuggestionDto(trimmed, uses));
                }

                List<MedicineSuggestionDto> list = new ArrayList<>(map.values());
                list.sort(Comparator.comparing(a -> a.name.toLowerCase(Locale.ROOT)));

                detailsCache = list;

                if (namesCache == null) {
                    namesCache = list.stream().map(d -> d.name).collect(Collectors.toList());
                }

                return detailsCache;

            } catch (Exception e) {
                detailsCache = List.of();
                if (namesCache == null) namesCache = List.of();
                return detailsCache;
            }
        }
    }

    private String buildUsesSummary(CSVRecord r) {
        List<String> uses = new ArrayList<>(5);

        for (int i = 0; i <= 4; i++) {
            String v = safeGet(r, "use" + i);
            if (v != null) {
                String t = v.trim();
                if (!t.isBlank()) uses.add(t);
            }
        }

        if (uses.isEmpty()) return "";

        if (uses.size() >= 2) return uses.get(0) + ", " + uses.get(1);
        return uses.get(0);
    }

    private String safeGet(CSVRecord r, String col) {
        try {
            return r.get(col);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
