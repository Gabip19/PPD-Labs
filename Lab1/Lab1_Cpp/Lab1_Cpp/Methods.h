#pragma once

extern int p;
extern int n;
extern int m;
extern int k;

extern int** matrix;
extern int** conv_matrix;
extern int** result;

void update_position(int x, int y);

void run_sequential();
void run_on_rows();
void run_on_columns();
void run_linear_distribution();
void run_cyclic_distribution();