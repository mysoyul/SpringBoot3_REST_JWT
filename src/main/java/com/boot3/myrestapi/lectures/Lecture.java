package com.boot3.myrestapi.lectures;

import com.boot3.myrestapi.security.userinfo.UserInfo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of="id")
@Entity
public class Lecture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    private LocalDateTime beginEnrollmentDateTime;

    private LocalDateTime closeEnrollmentDateTime;

    private LocalDateTime beginLectureDateTime;

    private LocalDateTime endLectureDateTime;
    
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;

    private boolean free;

    @Enumerated(EnumType.STRING)
    private LectureStatus lectureStatus = LectureStatus.DRAFT;

    @ManyToOne
    private UserInfo userInfo;

    public void update() {
        // Update free
        this.free = this.basePrice == 0 && this.maxPrice == 0;
        // Update offline
        this.offline = this.location != null && !this.location.isBlank();
    }

}    