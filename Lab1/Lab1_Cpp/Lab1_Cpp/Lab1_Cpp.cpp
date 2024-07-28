#include <chrono>
#include <fstream>
#include <iostream>
#include <string>
#include <vector>

#include "Methods.h"
using namespace std;

int p;
int n;
int m;
int k;

int** matrix;
int** conv_matrix;
int** result;

void read_matrix(int** arr, const int rows, const int columns, ifstream& fin)
{
    for (int i = 0; i < rows; i++) 
    {
        for (int j = 0; j < columns; j++) 
        {
            fin >> arr[i][j];
        }
    }
}

void read_input(const string& file_path)
{
    ifstream fin(file_path);

    fin >> n;
    fin >> m;
	matrix = new int*[n];
	for (int i = 0; i < n; ++i)
        matrix[i] = new int[m];

    read_matrix(matrix, n, m, fin);

    fin >> k;
    conv_matrix = new int* [n];
    for (int i = 0; i < n; ++i)
        conv_matrix[i] = new int[m];

    read_matrix(conv_matrix, k, k, fin);

    result = new int* [n];
    for (int i = 0; i < n; ++i)
        result[i] = new int[m];
}

void compare_results_with_sequential()
{
	const auto parallel_result = result;
    result = new int* [n];
    for (int i = 0; i < n; ++i)
        result[i] = new int[m];

    run_sequential();

    for (int i = 0; i < n; ++i)
    {
	    for (int j = 0; j < m; ++j)
	    {
		    if (parallel_result[i][j] != result[i][j])
		    {
                std::cerr << "Results do not match." << endl;
                return;
		    }
	    }
    }

    for (int i = 0; i < n; ++i)
    {
        delete parallel_result[i];
    }
    delete parallel_result;
}

void write_to_file()
{
    ofstream fout("output.txt");

    for (int i = 0; i < n; ++i)
    {
        for (int j = 0; j < m; ++j)
        {
            fout << result[i][j] << " ";
        }
        fout << endl;
    }
}

int main(int argc, char* argv[])
{
    p = stoi(argv[1]);
    const int file_number = stoi(argv[2]);
    const int run_option = stoi(argv[3]);
    const int check_result = stoi(argv[4]);

    const string file_path = "input" + to_string(file_number) + ".txt";

    read_input(file_path);

    const auto start = std::chrono::high_resolution_clock::now();

    switch (run_option)
	{
    case 0:
        run_sequential();
		break;
    case 1:
        run_on_rows();
		break;
    case 2:
        run_on_columns();
        break;
    case 3:
        run_linear_distribution();
        break;
    case 4:
        run_cyclic_distribution();
        break;
    default:
    	std::cerr << "Invalid run option." << std::endl;
        return 0;
    }

    const auto end = std::chrono::high_resolution_clock::now();
    const std::chrono::duration<double, std::milli> duration = end - start;
    std::cout << duration.count();

    if (check_result == 1)
    {
        compare_results_with_sequential();
        write_to_file();
    }

    for (int i = 0; i < n; ++i)
    {
        delete matrix[i];
    }
    delete matrix;

    for (int i = 0; i < n; ++i)
    {
        delete conv_matrix[i];
    }
    delete conv_matrix;

    for (int i = 0; i < n; ++i)
    {
        delete result[i];
    }
    delete result;
}
