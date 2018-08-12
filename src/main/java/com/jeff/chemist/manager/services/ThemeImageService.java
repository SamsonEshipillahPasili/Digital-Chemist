package com.jeff.chemist.manager.services;


/*
 * Handle all that pertakes to the Theme image
 */

import animatefx.animation.FadeIn;
import com.jeff.chemist.manager.repositories.ThemeImageRepository;
import javafx.scene.image.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThemeImageService {
    @Autowired
    private List<BackgroundImageObserver> backgroundImageObservers;

    @Autowired
    private ThemeImageRepository themeImageRepository;


    @Bean
    public List<BackgroundImageObserver> getBackgroundImageObservers(){
        return new ArrayList<>();
    }

    public void updateBackgroundImage(byte[] imageData){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
        this.backgroundImageObservers.forEach(backgroundImageObserver -> backgroundImageObserver.update());
    }

}
