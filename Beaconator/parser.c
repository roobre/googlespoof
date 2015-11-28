//
// Created by roobre on 11/28/15.
//

#include "parser.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void parse_aps(const char* filename, ap_t** list) {
    FILE* aps = fopen(filename, "r");
    char linebuf[64];
    ap_t** current;
    *list = NULL;

    current = list;
    while (fgets(linebuf, sizeof(linebuf), aps) != NULL) {
        (*current) = malloc(sizeof(ap_t));
        (*current)->next = NULL;

        if (*list == NULL)
            *list = *current;

        strcpy((*current)->essid, linebuf + 17);
        (*current)->essid[strlen((*current)->essid) - 1] = '\0'; // Wooo its magic

        for (int i = 0; i < 6; i++) {
            (*current)->bssid[i] = strtol(linebuf + 3*i, NULL, 16);
        }

        current = &((*current)->next);
    }
}

/*
                _
 You expected   \`*-.
 some docume-    )  _`-.
 ntation abo-   .  : `. .
 ut this po-    : _   '  \
 inter hell.   ; *` _.   `*-._
                `-.-'          `-.
 I can't gi-      ;       `       `.
 ve you tha-      :.       .        \
 t, so take       . \  .   :   .-'   .
 a cute cat       '  `+.;  ;  '      :
 instead.         :  '  |    ;       ;-.
                  ; '   : :`-:     _.`* ;
         [bug] .*' /  .*' ; .*`- +'  `*'
               `*-*   `*-*  `*-*'
 */
