#include <cstdio>
#include <fstream>
#include <iostream>
#include <mpi.h>
#include <string>

using namespace std;

int n;
int m;
int k;
int num_procs;

int** matrix;

MPI_Status status;


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

void free_matrix_memory(int** arr, const int rows)
{
	for (int i = 0; i < rows; ++i)
	{
		delete[] arr[i];
	}
	delete[] arr;
}

void read_and_send_conv_matrix(int**& conv_matrix, ifstream& fin)
{
	// Read conv_matrix
	fin >> k;
	conv_matrix = new int* [k];
	for (int i = 0; i < k; ++i)
		conv_matrix[i] = new int[k];
	read_matrix(conv_matrix, k, k, fin);

	// Broadcast k
	MPI_Bcast(&k, 1, MPI_INT, 0, MPI_COMM_WORLD);

	// Broadcast conv_matrix
	for (int i = 0; i < k; ++i)
	{
		MPI_Bcast(conv_matrix[i], k, MPI_INT, 0, MPI_COMM_WORLD);
	}
}

void receive_conv_matrix(int**& conv_matrix)
{
	// Receive k
	MPI_Bcast(&k, 1, MPI_INT, 0, MPI_COMM_WORLD);

	// Allocate memory for the conv_matrix in child processes
	conv_matrix = new int* [k];
	for (int i = 0; i < k; ++i)
		conv_matrix[i] = new int[k];

	// Receive conv_matrix
	for (int i = 0; i < k; ++i)
	{
		MPI_Bcast(conv_matrix[i], k, MPI_INT, 0, MPI_COMM_WORLD);
	}
}

void read_and_send_matrix(int rows_per_thread, ifstream& fin)
{
	int start = 0;

	MPI_Bcast(&rows_per_thread, 1, MPI_INT, 0, MPI_COMM_WORLD);
	MPI_Bcast(&m, 1, MPI_INT, 0, MPI_COMM_WORLD);

	matrix = new int* [rows_per_thread];
	for (int i = 0; i < rows_per_thread; ++i)
		matrix[i] = new int[m];

	for (int i = 1; i < num_procs; i++)
	{
		// Read matrix
		for (int _i = 0; _i < rows_per_thread; ++_i)
		{
			for (int _j = 0; _j < m; ++_j)
			{
				fin >> matrix[_i][_j];
			}
			MPI_Send(matrix[_i], m, MPI_INT, i, 0, MPI_COMM_WORLD);
		}
        
		start += rows_per_thread;
	}
}

void receive_matrix()
{
	MPI_Bcast(&n, 1, MPI_INT, 0, MPI_COMM_WORLD);
	MPI_Bcast(&m, 1, MPI_INT, 0, MPI_COMM_WORLD);

	matrix = new int* [n];
	for (int i = 0; i < n; ++i)
		matrix[i] = new int[m];

	for (int i = 0; i < n; ++i)
	{
		MPI_Recv(matrix[i], m, MPI_INT, 0, 0, MPI_COMM_WORLD, &status);
	}
}

void init_cache(const int my_id, int* const top_cache, int* const bottom_cache)
{
	if (my_id == 1)
	{
		MPI_Send(matrix[n - 1], m, MPI_INT, my_id + 1, 0, MPI_COMM_WORLD);

		for (int i = 0; i < m; ++i)
		{
			top_cache[i] = matrix[0][i];
		}
		MPI_Recv(bottom_cache, m, MPI_INT, my_id + 1, 0, MPI_COMM_WORLD, &status);
	}
	else if (my_id == num_procs - 1)
	{
		MPI_Send(matrix[0], m, MPI_INT, my_id - 1, 0, MPI_COMM_WORLD);

		for (int i = 0; i < m; ++i)
		{
			bottom_cache[i] = matrix[n - 1][i];
		}
		MPI_Recv(top_cache, m, MPI_INT, my_id - 1, 0, MPI_COMM_WORLD, &status);
	}
	else
	{
		MPI_Send(matrix[0], m, MPI_INT, my_id - 1, 0, MPI_COMM_WORLD);
		MPI_Send(matrix[n - 1], m, MPI_INT, my_id + 1, 0, MPI_COMM_WORLD);

		MPI_Recv(top_cache, m, MPI_INT, my_id - 1, 0, MPI_COMM_WORLD, &status);
		MPI_Recv(bottom_cache, m, MPI_INT, my_id + 1, 0, MPI_COMM_WORLD, &status);
	}
}

int compute_position_value(const int x, const int y, const int left, const int* prev_row, const int* next_row, int** conv_matrix)
{
	int sum = 0;

	const int left_index = y != 0 ? y - 1 : 0;
	const int right_index = y != m - 1 ? y + 1 : m - 1;

	sum += prev_row[left_index] * conv_matrix[0][0] + prev_row[y] * conv_matrix[0][1] + prev_row[right_index] * conv_matrix[0][2];
	sum += left * conv_matrix[1][0] + matrix[x][y] * conv_matrix[1][1] + matrix[x][right_index] * conv_matrix[1][2];
	sum += next_row[left_index] * conv_matrix[2][0] + next_row[y] * conv_matrix[2][1] + next_row[right_index] * conv_matrix[2][2];

	return sum;
}

void compute_and_send_matrix(int** conv_matrix, int* const top_cache, int* const bottom_cache)
{
	for (int i = 0; i < n; i++) {
		int left_elem = matrix[i][0];
		const int* next_row = i == n - 1 ? bottom_cache : matrix[i + 1];

		for (int j = 0; j < m; j++) {
			const int result = compute_position_value(i, j, left_elem, top_cache, next_row, conv_matrix);

			if (j > 0) {
				top_cache[j - 1] = left_elem;
			}

			left_elem = matrix[i][j];
			matrix[i][j] = result;
		}

		top_cache[m - 1] = left_elem;
	}

	for (int i = 0; i < n; ++i)
	{
		MPI_Send(matrix[i], m, MPI_INT, 0, 0, MPI_COMM_WORLD);
	}
}

void receive_and_write_matrix(const int rows_per_thread, const bool check_result, const string& check_path)
{
	ofstream fout("output.txt");
	ifstream check_fin(check_path);
	int value;

	for (int i = 1; i < num_procs; ++i)
	{
		for (int _i = 0; _i < rows_per_thread; ++_i)
		{
			MPI_Recv(matrix[_i], m, MPI_INT, i, 0, MPI_COMM_WORLD, &status);

			if (check_result)
			{
				for (int _j = 0; _j < m; ++_j)
				{
					fout << matrix[_i][_j] << " ";
					check_fin >> value;
					if (value != matrix[_i][_j])
					{
						cerr << "Results do not match!" << endl;
						return;
					}
				}
				fout << endl;
			}
		}
	}
}


int main(int argc, char* argv[])
{
	int my_id, name_len;
	char processor_name[MPI_MAX_PROCESSOR_NAME];

	MPI_Init(nullptr, nullptr);
	MPI_Comm_rank(MPI_COMM_WORLD, &my_id);
	MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
	MPI_Get_processor_name(processor_name, &name_len);

    int** conv_matrix = nullptr;

	const int file_number = stoi(argv[1]);
	const int check_result = stoi(argv[2]);

    const string in_path = "input" + to_string(file_number) + ".txt";
	const string check_path = "check" + to_string(file_number) + ".txt";

	// Read matrices, send values, write output
	if (my_id == 0)
	{
        std::ifstream fin(in_path);
        read_and_send_conv_matrix(conv_matrix, fin);

        fin >> n;
        fin >> m;

        const int rows_per_thread = n / (num_procs - 1);

		read_and_send_matrix(rows_per_thread, fin);

        receive_and_write_matrix(rows_per_thread, check_result == 1 ? true : false, check_path);

        free_matrix_memory(matrix, rows_per_thread);
	}
    // Process the received matrix part and send the response back to root
	else
	{
        receive_conv_matrix(conv_matrix);

        receive_matrix();

        const auto top_cache = new int[m];
        const auto bottom_cache = new int[m];

        init_cache(my_id, top_cache, bottom_cache);

        compute_and_send_matrix(conv_matrix, top_cache, bottom_cache);

        delete[] top_cache;
        delete[] bottom_cache;
        free_matrix_memory(matrix, n);
	}

    // Deallocate conv_matrix memory for all processes
    free_matrix_memory(conv_matrix, k);

	MPI_Finalize();
}
