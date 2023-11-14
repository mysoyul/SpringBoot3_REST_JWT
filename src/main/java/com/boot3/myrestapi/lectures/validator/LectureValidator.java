package com.boot3.myrestapi.lectures.validator;

import java.time.LocalDateTime;

import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class LectureValidator {
	public void validate(LectureReqDto lectureReqDto, Errors errors) {

		if(lectureReqDto.getBasePrice() > lectureReqDto.getMaxPrice() &&
				lectureReqDto.getMaxPrice() != 0) {
			//Field Error
			errors.rejectValue("basePrice", "wrongValue", "BasePrice is wrong");
			errors.rejectValue("maxPrice", "wrongValue", "MaxPrice is wrong");
			//Global Error
			errors.reject("wrongPrices", "BasePrice가 MaxPrice 보다 더 작은 값이어야 합니다.");
		}

		//강의종료 날짜
		LocalDateTime endLectureDateTime = lectureReqDto.getEndLectureDateTime();

		if(endLectureDateTime.isBefore(lectureReqDto.getBeginLectureDateTime()) ||
		   endLectureDateTime.isBefore(lectureReqDto.getCloseEnrollmentDateTime()) ||
		   endLectureDateTime.isBefore(lectureReqDto.getBeginEnrollmentDateTime()) ) {
			//Field Error
			errors.rejectValue("endLectureDateTime",
							"wrongValue",
						"endLectureDateTime(강의종료날짜)가 더 이후의 날짜이어야 합니다.");
		}
	}
}