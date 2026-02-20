package com.kce.AuthService.service;

public interface SequenceGeneratorService {
    long nextId(String seqName);
}