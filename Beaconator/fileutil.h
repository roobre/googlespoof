//
// Created by roobre on 11/29/15.
//

#ifndef BEACONATOR_FILEUTIL_H
#define BEACONATOR_FILEUTIL_H

extern const int MAX_BUFFER;

void save_str(const char* str, const char* path);
void load_str(char** str, const char* path);

#endif //BEACONATOR_FILEUTIL_H
