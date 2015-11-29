cmake_minimum_required(VERSION 3.4)
project(Beaconator)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/bin")

set(SOURCE_FILES main.c beaconator.c wigle.c)

link_libraries(orcon2 curl jansson)

add_executable(Beaconator ${SOURCE_FILES})