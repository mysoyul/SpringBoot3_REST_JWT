package com.boot3.myrestapi.lectures;

import com.boot3.myrestapi.common.exception.BusinessException;
import com.boot3.myrestapi.common.resource.ErrorsResource;
import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import com.boot3.myrestapi.lectures.dto.LectureResDto;
import com.boot3.myrestapi.lectures.dto.LectureResource;
import com.boot3.myrestapi.lectures.validator.LectureValidator;
import com.boot3.myrestapi.security.userinfo.UserInfo;
import com.boot3.myrestapi.security.userinfo.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@RestController
@RequestMapping(value = "/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    //Constructor Injection
//    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLecture(@PathVariable Integer id,
                                           @RequestBody @Valid LectureReqDto lectureReqDto,
                                           Errors errors,
                                           @CurrentUser UserInfo currentUser) {
        Optional<Lecture> optionalLecture = lectureRepository.findById(id);
        if (optionalLecture.isEmpty()) {
            throw new BusinessException(id + " Lecture Not Found", HttpStatus.NOT_FOUND);
        }

        if (errors.hasErrors()) {
            return getErrors(errors);
        }

        lectureValidator.validate(lectureReqDto, errors);
        if (errors.hasErrors()) {
            return getErrors(errors);
        }

        Lecture existingLecture = optionalLecture.get();
        if((existingLecture.getUserInfo() != null) && (!existingLecture.getUserInfo().equals(currentUser))) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        //ReqDto -> Entity
        this.modelMapper.map(lectureReqDto, existingLecture);
        existingLecture.update();
        Lecture savedLecture = this.lectureRepository.save(existingLecture);
        //Entity -> ResDto
        LectureResDto lectureResDto = modelMapper.map(savedLecture, LectureResDto.class);
        if(savedLecture.getUserInfo() != null)
            lectureResDto.setEmail(savedLecture.getUserInfo().getEmail());
        //ResDto -> Resource
        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getLecture(@PathVariable Integer id, @CurrentUser UserInfo currentUser) {
        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
        if (optionalLecture.isEmpty()) {
            //return ResponseEntity.notFound().build();
            throw new BusinessException(id + " Lecture Not Found", HttpStatus.NOT_FOUND);
        }

        Lecture lecture = optionalLecture.get();
        LectureResDto lectureResDto = modelMapper.map(lecture, LectureResDto.class);
        if (lecture.getUserInfo() != null)
            lectureResDto.setEmail(lecture.getUserInfo().getEmail());

        LectureResource lectureResource = new LectureResource(lectureResDto);
        //인증토큰의 email과 Lecture가 참조하는 email주소가 같으면 update 링크를 제공하기
        if ((lecture.getUserInfo() != null) && (lecture.getUserInfo().equals(currentUser))) {
            lectureResource.add(linkTo(LectureController.class)
                    .slash(lecture.getId()).withRel("update-lecture"));
        }
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> queryLectures(Pageable pageable,
                                           PagedResourcesAssembler<LectureResDto> assembler,
                                           @CurrentUser UserInfo currentUser) {
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        //Page<Lecture> => Page<LectureResDto>
        Page<LectureResDto> lectureResDtoPage =
                //Page 인터페이스 map(Function)
                lecturePage.map(lecture -> {
                    LectureResDto lectureResDto = new LectureResDto();
                    if (lecture.getUserInfo() != null) {
                        lectureResDto.setEmail(lecture.getUserInfo().getEmail());
                    }
                    modelMapper.map(lecture, lectureResDto);
                    return lectureResDto;
                });

        //PagedModel<EntityModel<LectureResDto>> pagedModel = assembler.toModel(lectureResDtoPage);
        /*
        PagedResourcesAssembler 의 toModel() 메서드
            public <R extends org.springframework.hateoas.RepresentationModel<?>>
            org.springframework.hateoas.PagedModel<R> toModel(Page<T> page,
                 org.springframework.hateoas.server.RepresentationModelAssembler<T,R> assembler)
        */
        /*
         RepresentationModelAssembler 의 추상 메서드
           D toModel(T entity)
           D, which extends RepresentationModel
         */
        PagedModel<LectureResource> pagedModel =
                //assembler.toModel(lectureResDtoPage, resDto -> new LectureResource(resDto));
                assembler.toModel(lectureResDtoPage, LectureResource::new);
        if (currentUser != null) {
            pagedModel.add(linkTo(LectureController.class).withRel("create-Lecture"));
        }
        return ResponseEntity.ok(pagedModel);
    }


    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto,
                                           Errors errors,
                                           @CurrentUser UserInfo currentUser) {
        //입력항목 검증
        if (errors.hasErrors()) {
            //status code 400
            return getErrors(errors);
        }
        //biz로직과 관련된 입력항목 검증
        this.lectureValidator.validate(lectureReqDto, errors);
        if (errors.hasErrors()) {
            return getErrors(errors);
        }

        //ReqDto -> Entity
        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);
        //offline, free 값을 update
        lecture.update();
        //Lecture와 UserInfo 연관관계 설정
        lecture.setUserInfo(currentUser);
        Lecture addLecture = this.lectureRepository.save(lecture);
        //Entity -> ResDto
        LectureResDto lectureResDto = modelMapper.map(addLecture, LectureResDto.class);
        lectureResDto.setEmail(addLecture.getUserInfo().getEmail());

        WebMvcLinkBuilder selfLinkBuilder =
                linkTo(LectureController.class).slash(lectureResDto.getId());
        URI createUri = selfLinkBuilder.toUri();

        LectureResource lectureResource = new LectureResource(lectureResDto);
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private static ResponseEntity<ErrorsResource> getErrors(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}