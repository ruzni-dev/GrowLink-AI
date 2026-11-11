package com.growlink.backend.service;

import java.util.List;
import java.util.Set;

record MatchingResult(int percentage, Set<String> matchedSkills, List<String> reasons) {}