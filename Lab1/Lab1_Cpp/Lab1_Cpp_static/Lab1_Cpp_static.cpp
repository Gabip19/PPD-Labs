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

int matrix[10000][10000];
int conv_matrix[5][5];
int result[10000][10000];

void read_input(const string& file_path)
{
    ifstream fin(file_path);
    fin >> n;
    fin >> m;

    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < m; j++)
        {
            fin >> matrix[i][j];
        }
    }

    fin >> k;

    for (int i = 0; i < k; i++)
    {
        for (int j = 0; j < k; j++)
        {
            fin >> conv_matrix[i][j];
        }
    }
}

void compare_results_with_sequential()
{
	const auto parallel_result = new int*[n];
    for (int i = 0; i < n; ++i)
    {
        parallel_result[i] = new int[m];
        for (int j = 0; j < m; ++j)
        {
            parallel_result[i][j] = result[i][j];
        }
    }

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
    delete[] parallel_result;
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

    ifstream fin(file_path);
    fin >> n;
    fin >> m;

    for (int i = 0; i < n; i++)
    {
        for (int j = 0; j < m; j++)
        {
            fin >> matrix[i][j];
        }
    }

    fin >> k;

    for (int i = 0; i < k; i++)
    {
        for (int j = 0; j < k; j++)
        {
            fin >> conv_matrix[i][j];
        }
    }

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
    printf("%f", duration.count());

    if (check_result == 1)
    {
    	compare_results_with_sequential();
        write_to_file();
    }
}
