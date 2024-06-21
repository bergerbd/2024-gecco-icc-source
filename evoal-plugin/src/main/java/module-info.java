module de.evoal.challenges.ic {
    // declare dependencies to evoal
    requires de.evoal.core.main;
    requires de.evoal.core.ea;

    // CDI related dependencies
    requires jakarta.inject.api;
    requires jakarta.enterprise.cdi.api;

    // logging
    requires org.slf4j;
    requires de.evoal.languages.model.base;
    requires static lombok;
    requires freemarker;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.jenetics.ext;
    requires io.jenetics.prog;
    requires de.evoal.languages.model.dl;
    requires de.evoal.languages.model.ddl;
    requires de.evoal.languages.model.instance;
    requires org.eclipse.emf.ecore;
    requires com.google.guice;
    requires de.evoal.languages.model.intance.dsl;
    requires org.eclipse.xtext;
    requires org.eclipse.emf.common;
    requires jdk.jdi;
    requires java.desktop;
    requires commons.math3;

    // open the package to CDI allowing CDI to create instances using reflection.
    // additionally, we have to open the folder to allow EvoAl to access the .dl file
    opens de.evoal.challenges.ic;
    opens de.evoal.challenges.ic.genotype to weld.core.impl;
}