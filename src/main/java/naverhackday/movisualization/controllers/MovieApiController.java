package naverhackday.movisualization.controllers;

import naverhackday.movisualization.dto.BoxOfficeRecord;
import naverhackday.movisualization.dto.MovieBoxOfficeResponse;
import naverhackday.movisualization.storage.BoxOfficeDao;
import naverhackday.movisualization.storage.BoxOfficeStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class MovieApiController {

    @Autowired
    private BoxOfficeStorageService boxOfficeRepository;

    @GetMapping("movie/{tmdbId}")
    public MovieBoxOfficeResponse showBoxoffice(@PathVariable String tmdbId) {

        return boxOfficeRepository.getListWithMovieCd(tmdbId);
    }

    @GetMapping("hello")
    public String hello() {
        return "Hello World";
    }
}
