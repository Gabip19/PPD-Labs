#include "Methods.h"

#include <algorithm>
#include <thread>

using namespace std;

void update_position(const int x, const int y)
{
    int sum = 0;
    const int middle = k / 2;
    for (int i = 0; i < k; i++)
    {
        for (int j = 0; j < k; j++)
        {
            int ii = x - middle + i;
            int jj = y - middle + j;

            ii = std::max(ii, 0);
            ii = std::min(ii, n - 1);

            jj = std::max(jj, 0);
            jj = std::min(jj, m - 1);

            sum += conv_matrix[i][j] * matrix[ii][jj];
        }
    }
    result[x][y] = sum;
}

void run_sequential()
{
	for (int i = 0; i < n; i++)
    {
		for (int j = 0; j < m; j++)
        {
			update_position(i, j);
		}
	}
}

void rows_func(const int start, const int end)
{
    for (int i = start; i < end; i++)
    {
        for (int j = 0; j < m; j++)
        {
            update_position(i, j);
        }
    }
}

void run_on_rows()
{
	auto* threads = new thread[p];
	const int rows_per_thread = n / p;
    int remaining_rows = n % p;
    int start = 0;
    int end;

    for (int i = 0; i < p; i++)
    {
        int current_rows_per_thread = rows_per_thread;

        if (remaining_rows != 0)
        {
            remaining_rows--;
            current_rows_per_thread++;
        }
        end = start + current_rows_per_thread;

        threads[i] = thread(rows_func, start, end);

        start += current_rows_per_thread;
    }

    for (int i = 0; i < p; i++)
    {
        threads[i].join();
    }
}

void column_func(const int start, const int end)
{
    for (int j = start; j < end; j++)
    {
        for (int i = 0; i < n; i++)
        {
            update_position(i, j);
        }
    }
}

void run_on_columns()
{
    auto* threads = new thread[p];
    const int columns_per_thread = m / p;
    int remaining_columns = m % p;
    int start = 0;
    int end;

    for (int i = 0; i < p; i++)
    {
        int current_columns_per_thread = columns_per_thread;

        if (remaining_columns != 0)
        {
            remaining_columns--;
            current_columns_per_thread++;
        }
        end = start + current_columns_per_thread;

        threads[i] = thread(column_func, start, end);

        start += current_columns_per_thread;
    }

    for (int i = 0; i < p; i++)
    {
        threads[i].join();
    }
}

void linear_func(const int start, const int end)
{
    for (int i = start; i < end; i++)
    {
	    const int row = i / m;
	    const int column = i % m;
        update_position(row, column);
    }
}

void run_linear_distribution()
{
    auto* threads = new thread[p];
    const int total_num_of_elems = n * m;
    const int elems_per_thread = total_num_of_elems / p;
    int remaining_elems = total_num_of_elems % p;
    int start = 0;
    int end;

    for (int i = 0; i < p; i++)
    {
        int current_elems_per_thread = elems_per_thread;

        if (remaining_elems != 0)
        {
            remaining_elems--;
            current_elems_per_thread++;
        }
        end = start + current_elems_per_thread;

        threads[i] = thread(linear_func, start, end);

        start += current_elems_per_thread;
    }

    for (int i = 0; i < p; i++)
    {
        threads[i].join();
    }
}

void cyclic_func(const int start, const int step)
{
	const int total_elems_num = n * m;

    for (int i = start; i < total_elems_num; i += step)
    {
	    const int row = i / m;
	    const int column = i % m;
        update_position(row, column);
    }
}

void run_cyclic_distribution()
{
    auto* threads = new thread[p];

    for (int i = 0; i < p; i++)
    {
        threads[i] = thread(cyclic_func, i, p);
    }

    for (int i = 0; i < p; i++)
    {
        threads[i].join();
    }
}
