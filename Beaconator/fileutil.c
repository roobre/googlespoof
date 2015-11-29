//
// Created by roobre on 11/29/15.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "fileutil.h"

const int MAX_BUFFER = 64 * 1024;

void save_str(const char* str, const char* path) {
    FILE* file = fopen(path, "w");
    fprintf(file, "%s", str);
    fclose(file);
}

void load_str(char** str, const char* path) {
    *str = malloc(MAX_BUFFER);
    FILE* file = fopen(path, "r");
    fgets(*str, MAX_BUFFER, file);
    *str = realloc(*str, strlen(*str));
}
