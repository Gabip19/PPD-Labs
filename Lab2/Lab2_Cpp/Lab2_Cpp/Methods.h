#pragma once
#include <barrier>

extern int p;
extern int n;
extern int m;
extern int k;

extern int** matrix;
extern int** conv_matrix;

int compute_position_value(int x, int y, int left, const int* prev_row, const int* next_row);

void run_sequential();
void run_on_rows(std::barrier<>& barrier);