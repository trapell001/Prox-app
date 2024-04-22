package com.prox.challenge.service;

import com.prox.challenge.model.*;
import com.prox.challenge.repository.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Log4j2
@Transactional
public class NowService {
    @Autowired
    private TrendingRepository trendingRepository;
    @Autowired
    private TrendingTypeRepository trendingTypeRepository;
    @Autowired
    private MusicRepository musicRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private FormIconRepository formIconRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AlphabetRepository alphabetRepository;

    public List<Trending> trendingFindAll(Optional<String> group, boolean edit, int version) {
        if (edit) {
            if (group.isPresent()) {
                return trendingRepository.findAllByGroupAndTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(group.get(), version);
            } else {
                return trendingRepository.findAllByTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(version);
            }
        } else {
            if (group.isPresent()) {
                return trendingRepository.findAllByShowAndGroupAndTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(true, group.get(),version);
            } else {
                return trendingRepository.findAllByShowAndTrendingType_RequiredVersionIsNullOrTrendingType_RequiredVersionLessThanEqualOrderByTopDesc(true, version);
            }
        }
    }

    public Trending trendingSave(Trending trending) {
        return trendingRepository.save(trending);
    }

    public void trendingDelete(Optional<String> id) {
        trendingRepository.deleteById(id.orElseThrow(() -> new RuntimeException("id requirement")));
    }

    public List<TrendingType> trendingTypeFindAll(int version) {
        return trendingTypeRepository.findAllByRequiredVersionIsNullOrRequiredVersionLessThanEqualOrderByRank(version);
    }

    public TrendingType trendingTypeSave(TrendingType trendingType) {
        return trendingTypeRepository.save(trendingType);
    }

    public void trendingTypeDelete(Optional<String> id) {
        trendingTypeRepository.deleteById(id.orElseThrow(() -> new RuntimeException("id requirement")));
    }

    public List<Music> musicFindAll() {
        return musicRepository.findAll();
    }

    public Music musicSave(Music music) {
        return musicRepository.save(music);
    }

    public void musicDelete(Optional<String> id) {
        musicRepository.deleteById(id.orElseThrow(() -> new RuntimeException("id requirement")));
    }


    public List<Image> imageFindAll(Optional<String> type) {
        if (type.isPresent()) {
            return imageRepository.findAllByType(type.get());
        } else {
            return imageRepository.findAll();
        }
    }

    public Image imageSave(Image image) {
        return imageRepository.save(image);
    }

    public void imageDelete(Optional<String> id) {
        Image image = imageRepository.findById(id.orElseThrow(() -> new RuntimeException("id requirement"))).orElseThrow(() -> new RuntimeException("Image Not Found"));
        imageRepository.delete(image);
    }
    // ------------------ FormIcon --------------------

    public List<FormIcon> formIconFindAll(int version) {
        return formIconRepository.findAllByRequiredVersionIsNullOrRequiredVersionLessThanEqual(version);
    }

    public FormIcon formIconSave(FormIcon formIcon) {
        if (formIcon.getAlphabets() != null) formIcon.setAlphabets(alphabetRepository.saveAll(formIcon.getAlphabets()));
        formIcon = formIconRepository.save(formIcon);
        return formIcon;
    }

    public FormIcon formIconSave(List<String> urls, String id) {
        FormIcon formIcon = formIconRepository.findById(id).orElseThrow(() -> new RuntimeException("FormIcon not found"));
        urls.forEach(s -> formIcon.getAlphabets().add(alphabetSave(new Alphabet().setUrl(s))));
        return formIconRepository.save(formIcon);
    }

    public FormIcon formIconSaveDeleteAlphabet(Long idAlphabet, String id) {
        FormIcon formIcon = formIconRepository.findById(id).orElseThrow(() -> new RuntimeException("FormIcon not found"));
        formIcon.getAlphabets().removeIf(alphabet -> alphabet.getId().equals(idAlphabet));
        alphabetRepository.deleteById(idAlphabet);
        return formIconRepository.save(formIcon);
    }

    public void formIconDelete(Optional<String> id) {
        FormIcon formIcon = formIconRepository.findById(id.orElseThrow(() -> new RuntimeException("id requirement"))).orElseThrow(() -> new RuntimeException("Call Icon notfound"));
        formIconRepository.delete(formIcon);
        alphabetRepository.deleteAll(formIcon.getAlphabets());
    }

    // ------------------ Question --------------------

    public List<Question> questionFindAll() {
        return questionRepository.findAll();
    }

    public Question questionSave(Question question) {
        question = questionRepository.save(question);
        return question;
    }

    public void questionDelete(Optional<Long> id) {
        questionRepository.deleteById(id.orElseThrow(() -> new RuntimeException("id requirement")));
    }

    // ------------------ Alphabet --------------------

    public List<Alphabet> alphabetFindAll() {
        return alphabetRepository.findAll();
    }

    public Alphabet alphabetSave(Alphabet alphabet) {
        alphabet = alphabetRepository.save(alphabet);
        return alphabet;
    }

    public void alphabetDelete(Optional<Long> id) {
        alphabetRepository.deleteById(id.orElseThrow(() -> new RuntimeException("id requirement")));
    }


}
