#include <barrier>
#include <chrono>
#include <fstream>
#include <iostream>
#include <string>

#include "Methods.h"
using namespace std;

int p;
int n;
int m;
int k;

int** matrix;
int** conv_matrix;

string file_path;

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

void read_input(const string& path)
{
    ifstream fin(path);

    fin >> n;
    fin >> m;
    matrix = new int* [n];
    for (int i = 0; i < n; ++i)
        matrix[i] = new int[m];

    read_matrix(matrix, n, m, fin);

    fin >> k;
    conv_matrix = new int* [k];
    for (int i = 0; i < k; ++i)
        conv_matrix[i] = new int[k];

    read_matrix(conv_matrix, k, k, fin);
}

void compare_results_with_sequential()
{
    const auto parallel_result = matrix;

    read_input(file_path);
    run_sequential();

    for (int i = 0; i < n; ++i)
    {
        for (int j = 0; j < m; ++j)
        {
            if (parallel_result[i][j] != matrix[i][j])
            {
                std::cerr << "Results do not match." << endl;
                return;
            }
        }
    }

    for (int i = 0; i < n; ++i)
    {
        delete[] parallel_result[i];
    }
    delete[] parallel_result;
}

void write_to_file()
{
    ofstream fout("output.txt");

    for (int i = 0; i < n; ++i)
    {
        for (int j = 0; j < m; ++j)
        {
            fout << matrix[i][j] << " ";
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

	file_path = "input" + to_string(file_number) + ".txt";

    std::barrier barrier{ p };

    read_input(file_path);

    const auto start = std::chrono::high_resolution_clock::now();

    switch (run_option)
    {
    case 0:
        run_sequential();
        break;
    case 1:
        run_on_rows(std::ref(barrier));
        break;
    default:
        std::cerr << "Invalid run option." << std::endl;
        return 0;
    }

    const auto end = std::chrono::high_resolution_clock::now();
    const std::chrono::duration<double, std::milli> duration = end - start;
    std::cout << duration.count() << endl;

    if (check_result == 1)
    {
	    write_to_file();
	    compare_results_with_sequential();
    }

    // for (int i = 0; i < n; ++i)
    // {
	   //  for (int j = 0; j < m; ++j)
	   //  {
    //         cout << matrix[i][j] << " ";
	   //  }
    //     cout << endl;
    // }

    for (int i = 0; i < n; ++i)
    {
        delete[] matrix[i];
    }
    delete[] matrix;

    for (int i = 0; i < k; ++i)
    {
        delete[] conv_matrix[i];
    }
    delete[] conv_matrix;
}
