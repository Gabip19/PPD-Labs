#pragma once

extern int p;
extern int n;
extern int m;
extern int k;

extern int matrix[10000][10000];
extern int conv_matrix[5][5];
extern int result[10000][10000];

void update_position(int x, int y);

void run_sequential();
void run_on_rows();
void run_on_columns();
void run_linear_distribution();
void run_cyclic_distribution();