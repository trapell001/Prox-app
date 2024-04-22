package com.prox.challenge.controller;

import com.prox.challenge.gcoder.model.Cover;
import com.prox.challenge.model.*;
import com.prox.challenge.service.NowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/now/v1")
public class TrendingController {
    @Autowired
    private NowService nowService;


    @GetMapping("/trending")
    @Cacheable(value = "challenge", key = "#httpServletRequest.requestURI + #httpServletRequest.queryString")
    public ResponseEntity<?> trendingFindAll(HttpServletRequest httpServletRequest,  @RequestParam("group") Optional<String> group,
                                             @RequestParam("edit") Optional<Boolean> edit, @RequestParam("version") Optional<Integer> version){
        return ResponseEntity.ok(new Cover(200, nowService.trendingFindAll(group, edit.orElse(false), version.orElse(1))));
    }
    @PostMapping("/trending")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> trendingSave(@RequestBody Trending trending){
        return ResponseEntity.ok(new Cover(200, nowService.trendingSave(trending)));
    }
    @DeleteMapping("/trending/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> trendingDelete(@PathVariable Optional<String> id){
        nowService.trendingDelete(id);
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @GetMapping("/trendingType")
    @Cacheable(value = "challenge", key = "#httpServletRequest.requestURI + #httpServletRequest.queryString")
    public ResponseEntity<?> trendingTypeFindAll(HttpServletRequest httpServletRequest,  @RequestParam("version") Optional<Integer> version){
        return ResponseEntity.ok(new Cover(200, nowService.trendingTypeFindAll(version.orElse(1))));
    }
    @PostMapping("/trendingType")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> trendingTypeSave(@RequestBody TrendingType trendingType){
        return ResponseEntity.ok(new Cover(200, nowService.trendingTypeSave(trendingType)));
    }
    @DeleteMapping("/trendingType/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> trendingTypeDelete(@PathVariable Optional<String> id){
        nowService.trendingTypeDelete(id);
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @GetMapping("/music")
    @Cacheable(value = "challenge", key = "#httpServletRequest.requestURI + #httpServletRequest.queryString")
    public ResponseEntity<?> musicFindAll(HttpServletRequest httpServletRequest){
        return ResponseEntity.ok(new Cover(200, nowService.musicFindAll()));
    }
    @PostMapping("/music")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> musicSave(@RequestBody Music music){
        return ResponseEntity.ok(new Cover(200, nowService.musicSave(music)));
    }
    @DeleteMapping("/music/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> musicDelete(@PathVariable Optional<String> id){
        nowService.musicDelete(id);
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    @Cacheable(value = "challenge", key = "#httpServletRequest.requestURI + #httpServletRequest.queryString")
    @GetMapping("/image")
    public ResponseEntity<?> imageFindAll(HttpServletRequest httpServletRequest,  @RequestParam("type") Optional<String> type){
        return ResponseEntity.ok(new Cover(200, nowService.imageFindAll(type)));
    }
    @PostMapping("/image")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> imageSave(@RequestBody Image image){
        return ResponseEntity.ok(new Cover(200, nowService.imageSave(image)));
    }
    @DeleteMapping("/image/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> imageDelete(@PathVariable Optional<String> id){
        nowService.imageDelete(id);
        return ResponseEntity.ok(new Cover(200, "done"));
    }
    //==================== FormIcon ==================
    @Cacheable(value = "taptap", key = "#httpServletRequest.requestURI + #httpServletRequest.queryString")
    @GetMapping("/formIcon")
    public ResponseEntity<?> formIconFindAll(HttpServletRequest httpServletRequest, @RequestParam("version") Optional<Integer> version){
        List<FormIcon> formIcons = nowService.formIconFindAll(version.orElse(1));
        return ResponseEntity.ok(new Cover(200, formIcons));
    }
    @PostMapping("/formIcon")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> formIconSave(@RequestBody FormIcon formIcon){
        return ResponseEntity.ok(new Cover(200, nowService.formIconSave(formIcon)));
    }
    @PostMapping("/formIcon-addAlphabet/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> formIconSave(@RequestBody List<String> urls, @PathVariable String id){
        return ResponseEntity.ok(new Cover(200, nowService.formIconSave(urls, id)));
    }
    @PostMapping("/formIcon-deleteAlphabet/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> formIconSave(@RequestBody Long idAlphabet, @PathVariable String id){
        return ResponseEntity.ok(new Cover(200, nowService.formIconSaveDeleteAlphabet(idAlphabet, id)));
    }
    @DeleteMapping("/formIcon/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> formIconDelete(@PathVariable Optional<String> id){
        nowService.formIconDelete(id);
        return ResponseEntity.ok(new Cover(200, "done"));
    }
    //==================== Question ==================
    @Cacheable(value = "challenge", key = "#httpServletRequest.requestURI + #httpServletRequest.queryString")
    @GetMapping("/question")
    public ResponseEntity<?> questionFindAll(HttpServletRequest httpServletRequest){
        List<Question> questions = nowService.questionFindAll();
        return ResponseEntity.ok(new Cover(200, questions));
    }
    @PostMapping("/question")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> questionSave(@RequestBody Question question){
        return ResponseEntity.ok(new Cover(200, nowService.questionSave(question)));
    }
    @DeleteMapping("/question/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> questionDelete(@PathVariable Optional<Long> id){
        nowService.questionDelete(id);
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    //==================== Alphabet ==================
    @Cacheable(value = "challenge", key = "#httpServletRequest.requestURI + #httpServletRequest.queryString")
    @GetMapping("/alphabet")
    public ResponseEntity<?> alphabetFindAll(HttpServletRequest httpServletRequest){
        List<Alphabet> alphabets = nowService.alphabetFindAll();
        return ResponseEntity.ok(new Cover(200, alphabets));
    }
    @PostMapping("/alphabet")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> alphabetSave(@RequestBody Alphabet alphabet){
        return ResponseEntity.ok(new Cover(200, nowService.alphabetSave(alphabet)));
    }
    @DeleteMapping("/alphabet/{id}")
    @CacheEvict(value = "challenge", allEntries = true)
    public ResponseEntity<?> alphabetDelete(@PathVariable Optional<Long> id){
        nowService.alphabetDelete(id);
        return ResponseEntity.ok(new Cover(200, "done"));
    }

    // reset cache
    @CacheEvict(value = "challenge", allEntries = true)
    @GetMapping("/reset-cache")
    public void resetCache(){}

}
