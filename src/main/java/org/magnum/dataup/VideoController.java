package org.magnum.dataup;


import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class VideoController {

    private List<Video> videos = new ArrayList<>();
    private VideoFileManager videoFileManager = VideoFileManager.get();

    public VideoController() throws IOException {
    }

    @GetMapping("/video")
    public ResponseEntity<List<Video>> getVideo() {
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @PostMapping("/video")
    public ResponseEntity<Video> uploadVideo(@RequestBody Video video) throws IOException {
        List<Long> idList = VideoFileManager.getCurrentVideoIDList();
        idList.addAll(videos.stream().map(Video::getId).collect(Collectors.toList()));
        OptionalLong max = idList.stream().mapToLong(Long::longValue).max();
        if (!max.isPresent()) {
            video.setId(1L);
        } else {
            video.setId(max.getAsLong() + 1);
        }
        videos.add(video);
        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @PostMapping("/video/{id}/data")
    public ResponseEntity<VideoStatus> uploadVideoBinary(@PathVariable("id") long videoId, @RequestParam("data") MultipartFile data) {
        Optional<Video> vid = videos.stream().filter(v -> v.getId() == videoId).findAny();
        if (!vid.isPresent()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } else {
            try {
                videoFileManager.saveVideoData(vid.get(), data.getInputStream());
                return new ResponseEntity<>(new VideoStatus(VideoStatus.VideoState.READY), HttpStatus.OK);
            } catch (IOException e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}