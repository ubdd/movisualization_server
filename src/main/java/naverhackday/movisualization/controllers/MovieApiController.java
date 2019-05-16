package naverhackday.movisualization.controllers;

import naverhackday.movisualization.client.KobisClient;
import naverhackday.movisualization.client.TMDBClient;
import naverhackday.movisualization.dto.*;
import naverhackday.movisualization.exception.InvalidDateRangeException;
import naverhackday.movisualization.storage.BoxOfficeDao;
import naverhackday.movisualization.storage.BoxOfficeStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class MovieApiController {

    @Autowired
    private BoxOfficeStorageService boxOfficeRepository;

    private KobisClient kobisClient = new KobisClient();
    private TMDBClient tmdbClient = new TMDBClient();

    @GetMapping("movie/{tmdbId}")
    public MovieBoxOfficeResponse showBoxoffice(@PathVariable String tmdbId) {

        return boxOfficeRepository.getListWithMovieCd(tmdbId);
    }

    @GetMapping("boxoffice")
    public List<DailyBoxOfficeResult> showBoxofficeWithDateRange(@RequestParam(required = false) String from_dt, @RequestParam(required = false) String to_dt) {

        if ((from_dt == null) || (to_dt == null)) {
            throw new InvalidDateRangeException("Date is not valid");
        }

        List<BoxOfficeRecord> boxOfficeRecordList = boxOfficeRepository.getWithDateRange(from_dt, to_dt);
        Map<String, List<BoxOfficeResult>> dateMap = new HashMap<>();
        List<DailyBoxOfficeResult> result = new ArrayList<>();

        for (BoxOfficeRecord e : boxOfficeRecordList) {
            if (!dateMap.containsKey(e.getCurrentDate())){
                dateMap.put(e.getCurrentDate(), new ArrayList<>());
            }

            BoxOfficeResult boxOfficeResult = new BoxOfficeResult();

            boxOfficeResult.setAudi_cnt(e.getAudiCnt());
            boxOfficeResult.setMovie_nm(e.getMovieNm());
            boxOfficeResult.setRank(e.getRank());
            boxOfficeResult.setRank_inten(e.getRankInten());
            boxOfficeResult.setRank_old_and_new(e.getRankOldAndNew());
            boxOfficeResult.setSales_amt(e.getSalesAmt());
            boxOfficeResult.setTmdb_movie_id(e.getTmdbId());
            boxOfficeResult.setTotal_rank(e.getTotalRank());
            boxOfficeResult.setAudi_acc(e.getAudiAcc());

            dateMap.get(e.getCurrentDate()).add(boxOfficeResult);
        }

        List<String> keyList = new ArrayList<>(dateMap.keySet());
        keyList.sort(Comparator.naturalOrder());

        for (String date : keyList) {
            DailyBoxOfficeResult dailyBoxOfficeResult = new DailyBoxOfficeResult();
            dailyBoxOfficeResult.setDate(date);

            dateMap.get(date).sort((a, b) -> a.getTotal_rank() - b.getTotal_rank());
            dailyBoxOfficeResult.setBox_office_result(new ArrayList<>(dateMap.get(date)));

            result.add(dailyBoxOfficeResult);
        }

        return result;

    }

    @GetMapping("top_movie_records")
    public List<TopMovie> topMovieRecords(@RequestParam(required = false) String from_dt, @RequestParam(required = false) String to_dt) {

        if ((from_dt == null) || (to_dt == null)) {
            throw new InvalidDateRangeException("Date is not valid");
        }

        return boxOfficeRepository.getTopMovies(from_dt, to_dt);
    }

    @GetMapping("find/{peopleNm}")
    public String getCode(@PathVariable String peopleNm) {
        return kobisClient.getPeopleCd(peopleNm);
    }

    @GetMapping("person_stat/{personId}")
    public PersonStat personStat(@PathVariable String personId) {
        List<TMDBCast> castList = tmdbClient.getCasts(personId);
        double popularity = tmdbClient.getPopularity(personId);

        double totalVotePoint = 0;
        double totalVoteAvg = 0;
        int totalVoteCount = 0;

        for (TMDBCast cast : castList) {
            totalVoteCount += cast.getVoteCount();
            totalVotePoint += cast.getVoteCount() * cast.getVoteAverage();
        }

        totalVoteAvg = totalVotePoint / totalVoteCount;
        int filmoCount = castList.size();

        PersonStat personStat = new PersonStat();

        personStat.setAvg_rate(totalVoteAvg);
        personStat.setFilmo_count(filmoCount);
        personStat.setPopularity(popularity);

        return personStat;

    }

    @GetMapping("hello")
    public String hello() {
        return "Hello World";
    }
}
