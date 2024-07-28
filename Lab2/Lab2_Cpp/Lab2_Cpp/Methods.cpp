#include "Methods.h"

#include <algorithm>
#include <barrier>
#include <thread>

using namespace std;

int compute_position_value(const int x, const int y, const int left, const int* prev_row, const int* next_row)
{
    int sum = 0;

    const int left_index = y != 0 ? y - 1 : 0;
    const int right_index = y != m - 1 ? y + 1 : m - 1;

    sum += prev_row[left_index] * conv_matrix[0][0] + prev_row[y] * conv_matrix[0][1] + prev_row[right_index] * conv_matrix[0][2];
    sum += left * conv_matrix[1][0] + matrix[x][y] * conv_matrix[1][1] + matrix[x][right_index] * conv_matrix[1][2];
    sum += next_row[left_index] * conv_matrix[2][0] + next_row[y] * conv_matrix[2][1] + next_row[right_index] * conv_matrix[2][2];

    return sum;
}

void run_sequential()
{
	const auto prev_row = new int[m];
	const auto last_row = new int[m];

    for (int j = 0; j < m; j++) {
        prev_row[j] = matrix[0][j];
        last_row[j] = matrix[n - 1][j];
    }

    for (int i = 0; i < n; i++) {
        int left_elem = matrix[i][0];
        const int* next_row = i == n - 1 ? last_row : matrix[i + 1];

        for (int j = 0; j < m; j++) {
	        const int result = compute_position_value(i, j, left_elem, prev_row, next_row);

            if (j > 0) {
                prev_row[j - 1] = left_elem;
            }

            left_elem = matrix[i][j];
            matrix[i][j] = result;
        }

        prev_row[m - 1] = left_elem;
    }

    delete[] prev_row;
    delete[] last_row;
}

void init_cache(const int start_row, const int end_row, int*& prev_row, int*& last_row)
{
    prev_row = new int[m];
    last_row = new int[m];

    const int prev_row_index = start_row == 0 ? start_row : start_row - 1;
    const int last_row_index = end_row == n ? n - 1 : end_row;

    for (int j = 0; j < m; j++) {
        prev_row[j] = matrix[prev_row_index][j];
        last_row[j] = matrix[last_row_index][j];
    }
}

void rows_func(const int start, const int end, std::barrier<>& barrier)
{
    int* prev_row;
    int* last_row;
    init_cache(start, end, std::ref(prev_row), std::ref(last_row));

	barrier.arrive_and_wait();

    for (int i = start; i < end; i++) {
        int left_elem = matrix[i][0];
        const int* next_row = i == end - 1 ? last_row : matrix[i + 1];

        for (int j = 0; j < m; j++) {
	        const int result = compute_position_value(i, j, left_elem, prev_row, next_row);

            if (j > 0) {
                prev_row[j - 1] = left_elem;
            }

            left_elem = matrix[i][j];
            matrix[i][j] = result;
        }

        prev_row[m - 1] = left_elem;
    }
}

void run_on_rows(std::barrier<>& barrier)
{
    auto* threads = new thread[p];
    const int rows_per_thread = n / p;
    int remaining_rows = n % p;
    int start = 0;

    for (int i = 0; i < p; i++)
    {
        int current_rows_per_thread = rows_per_thread;

        if (remaining_rows != 0)
        {
            remaining_rows--;
            current_rows_per_thread++;
        }
        int end = start + current_rows_per_thread;

        threads[i] = thread(rows_func, start, end, std::ref(barrier));

        start += current_rows_per_thread;
    }

    for (int i = 0; i < p; i++)
    {
        threads[i].join();
    }
}