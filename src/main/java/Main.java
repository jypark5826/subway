import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    static List<Line> lineDao = new ArrayList<>();
    static List<Section> sectionDao = new ArrayList<>();

    public static void main(String[] args) {
        // Line 최초 생성: 0 노선에서 0 -> 1의 구간이 주어짐 (거리 5)
        saveLine(new Line(0L, 0L, 1L, 5));
        saveSection(new Section(0L, 0L, 0L, 1L, 5));


        // 상행 종점 변경: 2 -> 0의 구간이 주어짐(거리 5)
        SectionRequest sectionRequest = new SectionRequest(0L, 2L, 0L, 5);
        Line line = findLineById(0L);
        List<Section> sections = findSectionsByLineId(0L);
        Map<Long, Section> orderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getUpStationId, section -> section));
        Map<Long, Section> reverseOrderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getDownStationId, section -> section));

        if (sectionRequest.getUpStationId().equals(sectionRequest.getDownStationId())) {
            throw new RuntimeException();
        }

        if (상행종점변경(line, sectionRequest, reverseOrderedSections)) {
            System.out.println("상행 종점 변경: 2 -> 0의 구간이 주어짐(거리 5)");
            printStations(line);
        }


        // 하행 종점 변경: 1 -> 3의 구간이 주어짐(거리 5)
        sectionRequest = new SectionRequest(0L, 1L, 3L, 5);
        line = findLineById(0L);
        sections = findSectionsByLineId(0L);
        orderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getUpStationId, section -> section));
        reverseOrderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getDownStationId, section -> section));

        if (sectionRequest.getUpStationId().equals(sectionRequest.getDownStationId())) {
            throw new RuntimeException();
        }

        if (하행종점변경(line, sectionRequest, orderedSections)) {
            System.out.println("하행 종점 변경: 1 -> 3의 구간이 주어짐(거리 5)");
            printStations(line);
        }


        // 하행역이 새로 추가되는 경우: 0 -> 4의 구간이 주어짐(거리 7)
        sectionRequest = new SectionRequest(0L, 0L, 4L, 7);
        line = findLineById(0L);
        sections = findSectionsByLineId(0L);
        orderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getUpStationId, section -> section));
        reverseOrderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getDownStationId, section -> section));

        if (sectionRequest.getUpStationId().equals(sectionRequest.getDownStationId())) {
            throw new RuntimeException();
        }

        if (하행역추가(sectionRequest, orderedSections)) {
            System.out.println("하행역이 새로 추가되는 경우: 0 -> 4의 구간이 주어짐(거리 7)");
            printStations(line);
        }


        // 상행역이 새로 추가되는 경우: 5 -> 1의 구간이 주어짐(거리 7)
        sectionRequest = new SectionRequest(0L, 5L, 1L, 7);
        line = findLineById(0L);
        sections = findSectionsByLineId(0L);
        orderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getUpStationId, section -> section));
        reverseOrderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getDownStationId, section -> section));

        if (sectionRequest.getUpStationId().equals(sectionRequest.getDownStationId())) {
            throw new RuntimeException();
        }

        if (상행역추가(sectionRequest, reverseOrderedSections)) {
            System.out.println("상행역이 새로 추가되는 경우: 5 -> 1의 구간이 주어짐(거리 7)");
            printStations(line);
        }
    }

    static boolean 상행종점변경(Line line, SectionRequest sectionRequest, Map<Long, Section> reverseOrderedSections) {
        if (sectionRequest.getDownStationId().equals(line.getUpStationId()) && !reverseOrderedSections.containsKey(sectionRequest.getUpStationId())) {
            // LineDao에서 해당 라인의 upStationId와 distance를 업데이트
            Line updateLine = new Line(line.getId(),
                    sectionRequest.getUpStationId(),
                    line.getDownStationId(),
                    line.getDistance() + sectionRequest.getDistance());
            updateLine(line.getId(), updateLine);

            // SectionDao에서 구간 추가
            Section newSection = new Section(1L, 0L, sectionRequest.getUpStationId(), sectionRequest.getDownStationId(), sectionRequest.getDistance());
            saveSection(newSection);
            return true;
        }
        return false;
    }

    static boolean 하행종점변경(Line line, SectionRequest sectionRequest, Map<Long, Section> orderedSections) {
        if (sectionRequest.getUpStationId().equals(line.getDownStationId()) && !orderedSections.containsKey(sectionRequest.getDownStationId())) {
            // LineDao에서 해당 라인의 downStationId와 distance를 업데이트
            Line updateLine = new Line(line.getId(),
                    line.getUpStationId(),
                    sectionRequest.getDownStationId(),
                    line.getDistance() + sectionRequest.getDistance());
            updateLine(line.getId(), updateLine);

            // SectionDao에서 구간 추가
            Section newSection = new Section(2L,
                    0L,
                    sectionRequest.getUpStationId(),
                    sectionRequest.getDownStationId(),
                    sectionRequest.getDistance());
            saveSection(newSection);
            return true;
        }
        return false;
    }

    static boolean 하행역추가(SectionRequest sectionRequest, Map<Long, Section> orderedSections) {
        Long upStationId = sectionRequest.getUpStationId();
        int distanceSum = 0;

        while (orderedSections.containsKey(upStationId)) {
            Section section = orderedSections.get(upStationId);

            if (distanceSum + section.getDistance() > sectionRequest.getDistance()) {
                Section newSection = new Section(3L,
                        0L,
                        upStationId,
                        sectionRequest.getDownStationId(),
                        sectionRequest.getDistance() - distanceSum);
                saveSection(newSection);

                Section updateSection = new Section(4L,
                        0L,
                        sectionRequest.getDownStationId(),
                        section.getDownStationId(),
                        distanceSum + section.getDistance() - sectionRequest.getDistance());
                updateSection(section.getId(), updateSection);
                return true;
            }

            upStationId = section.getDownStationId();
            distanceSum += section.getDistance();
        }
        return false;
    }

    static boolean 상행역추가(SectionRequest sectionRequest, Map<Long, Section> reverseOrderedSections) {
        Long downStationId = sectionRequest.getDownStationId();
        int distanceSum = 0;

        while (reverseOrderedSections.containsKey(downStationId)) {
            Section section = reverseOrderedSections.get(downStationId);

            if (distanceSum + section.getDistance() > sectionRequest.getDistance()) {
                Section newSection = new Section(5L,
                        0L,
                        sectionRequest.getUpStationId(),
                        downStationId,
                        sectionRequest.getDistance() - distanceSum);
                saveSection(newSection);

                Section updateSection = new Section(6L,
                        0L,
                        section.getUpStationId(),
                        sectionRequest.getUpStationId(),
                        distanceSum + section.getDistance() - sectionRequest.getDistance());
                updateSection(section.getId(), updateSection);
                return true;
            }

            downStationId = section.getUpStationId();
            distanceSum += section.getDistance();
        }
        return false;
    }

    static void saveLine(Line line) {
        lineDao.add(line);
    }

    static void saveSection(Section section) {
        sectionDao.add(section);
    }

    static Line findLineById(Long id) {
        return lineDao.stream()
                .filter(line -> line.getId().equals(id))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    static List<Section> findSectionsByLineId(Long id) {
        return sectionDao.stream()
                .filter(section -> section.getLineId().equals(id))
                .collect(Collectors.toList());
    }

    static Section findSectionById(Long id, Long stationId) {
        return sectionDao.stream()
                .filter(section -> section.getLineId().equals(stationId))
                .filter(section -> section.getId().equals(id))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    static void updateLine(Long id, Line updateLine) {
        Line line = findLineById(id);
        line.setUpStationId(updateLine.getUpStationId());
        line.setDownStationId(updateLine.getDownStationId());
        line.setDistance(updateLine.getDistance());
    }

    static void updateSection(Long id, Section updateSection) {
        Section section = findSectionById(id, updateSection.getLineId());
        section.setUpStationId(updateSection.getUpStationId());
        section.setDownStationId(updateSection.getDownStationId());
        section.setDistance(updateSection.getDistance());
    }

    static void printStations(Line line) {
        List<Long> stations = new ArrayList<>();
        List<Section> sections = findSectionsByLineId(line.getId());
        Map<Long, Section> orderedSections = sections.stream()
                .collect(Collectors.toMap(Section::getUpStationId, section -> section));
        Long upStationId = line.getUpStationId();
        stations.add(upStationId);

        while (orderedSections.containsKey(upStationId)) {
            Section section = orderedSections.get(upStationId);
            stations.add(section.getDownStationId());
            upStationId = section.getDownStationId();
            System.out.println(section.getUpStationId() + " -> " + section.getDownStationId() + " (" + section.getDistance() + ")");
        }

        System.out.println("stations: " + stations);
        System.out.println();
    }
}
