package com.boot3.myrestapi.common.resource;

import com.boot3.myrestapi.common.controller.IndexController;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsResource extends EntityModel<Errors> {
    //@JsonUnwrapped
    private Errors errors;

    public ErrorsResource(Errors content) {
        this.errors = content;
        //http://localhost:8080/api
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }

    public Errors getErrors() {
        return errors;
    }
}