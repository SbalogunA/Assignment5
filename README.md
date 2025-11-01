# Assignment 5 – Unit, Mocking and Integration Testing


## Project Overview


This project focuses on unit testing, mocking, integration testing, and continuous integration (CI) using JUnit 5, Mockito, and GitHub Actions. The goal is to strengthen automated testing practices and ensure consistent code quality through CI pipelines.


The assignment is divided into three main parts:


Part 1 – Testing Practice:
Write both specification-based and structural-based tests for the BarnesAndNoble package. These tests verify program correctness through behavior and structure while using appropriate annotations such as @DisplayName("specification-based") and @DisplayName("structural-based").


Part 2 – Continuous Integration Workflow:
Configure a GitHub Actions workflow (.github/workflows/SE333_CI.yml) that automatically runs Checkstyle (for static analysis) and JaCoCo (for code coverage) every time code is pushed to the main branch. The workflow uploads both reports as build artifacts and displays a status badge in the README.


Part 3 – Advanced Testing (Amazon Package):
Implement both integration tests and unit tests for the Amazon package, covering interactions between multiple components as well as isolated class-level behavior. Tests should use mocks where appropriate and ensure full branch and functional coverage.


The repository demonstrates best practices in test automation, CI configuration, and code quality enforcement. All responses and reflections are documented directly within this README.md file rather than a separate PDF submission
