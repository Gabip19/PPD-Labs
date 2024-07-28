#include <cuda_runtime.h>
#include <device_launch_parameters.h>
#include <stdio.h>
#include <fstream>
#include <iostream>
#include <chrono>
#include "kernel.h"

using namespace std;

#define KERNEL_SIZE 3

__global__ void convolutionKernel(int* f, const int* c, int* v, int width, int height)
{
    int i = blockIdx.x * blockDim.x + threadIdx.x;
    int j = blockIdx.y * blockDim.y + threadIdx.y;

    if (i < height && j < width)
    {
        int result = 0;

        for (int m = 0; m < KERNEL_SIZE; ++m)
        {
            for (int n = 0; n < KERNEL_SIZE; ++n)
            {
                int row = i + m - 1;
                int col = j + n - 1;

                // Check boundaries
                if (row >= 0 && row < height && col >= 0 && col < width)
                {
                    result += f[row * width + col] * c[m * KERNEL_SIZE + n];
                }
                else
                {
                    // Handle border conditions
                    if (row == -1)
                        row = 0;
                    else if (row == height)
                        row = height - 1;

                    if (col == -1)
                        col = 0;
                    else if (col == width)
                        col = width - 1;

                    result += f[row * width + col] * c[m * KERNEL_SIZE + n];
                }
            }
        }
        printf("i:%d j:%d result:%d\n", i, j, result);
        v[i * width + j] = result;
    }
}

void generateRandomMatrixToFile(int n, int m, int minValue, int maxValue, const string& fileName) {

    srand(time(0));

    try {
        ofstream file(fileName);

        if (file.is_open()) {

            file << n << " " << m << "\n";


            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    int randomValue = rand() % (maxValue - minValue + 1) + minValue;
                    file << randomValue << " ";
                }
                file << "\n";
            }

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    file << rand() % 2 << " ";
                }
                file << "\n";
            }

            file.close();
        }
        else {
            cerr << "Unable to open the file: " << fileName << endl;
        }
    }
    catch (const exception& e) {
        cerr << "Error: " << e.what() << endl;
    }
}

void readMatrixFromFile(const char* filename, int*& matrix, int*& result,int* kernel, int& width, int& height)
{
    FILE* file = fopen(filename, "r");
    if (!file)
    {
        printf("Error opening file.\n");
        exit(EXIT_FAILURE);
    }

    fscanf(file, "%d %d", &width, &height);

    matrix = new int[width * height];
    result = new int[width * height];

    for (int i = 0; i < height; ++i)
    {
        for (int j = 0; j < width; ++j)
        {
            fscanf(file, "%d", &matrix[i * width + j]);
        }
    }

    for (int i = 0; i < KERNEL_SIZE; ++i)
    {
        for (int j = 0; j < KERNEL_SIZE; ++j)
        {
            fscanf(file, "%d", &kernel[i * KERNEL_SIZE + j]);
        }
    }

    fclose(file);
}

void writeResultToFile(int height, int width, int* v)
{

    std::ofstream outFile("output.txt");
    if (!outFile.is_open())
    {
        std::cerr << "Error opening file for writing." << std::endl;
        return;
    }

    for (int i = 0; i < height; ++i)
    {
        for (int j = 0; j < width; ++j)
        {
            outFile << v[i * width + j] << " ";
        }
        outFile << std::endl;
    }

    outFile.close();

}

int main()
{   
    int width, height;
    int* f;
    // Result matrix
    int* v;
    int c[KERNEL_SIZE * KERNEL_SIZE];

    //generateRandomMatrixToFile(10, 10, 0, 10, "data1.txt");

    // Read the matrix from the file
    readMatrixFromFile("input_1.txt", f, v, c, width, height);

    int* dev_f, * dev_c, * dev_v;

    // Allocate device memory
    cudaMalloc((void**)&dev_f, width * height * sizeof(int));
    cudaMalloc((void**)&dev_c, KERNEL_SIZE * KERNEL_SIZE * sizeof(int));
    cudaMalloc((void**)&dev_v, width * height * sizeof(int));

    // Copy data from host to device
    cudaMemcpy(dev_f, f, width * height * sizeof(int), cudaMemcpyHostToDevice);
    cudaMemcpy(dev_c, c, KERNEL_SIZE * KERNEL_SIZE * sizeof(int), cudaMemcpyHostToDevice);

    // Set up grid and block dimensions
    dim3 blockSize(16, 16); //2x2 threads per block
    dim3 gridSize((width + blockSize.x - 1) / blockSize.x, (height + blockSize.y - 1) / blockSize.y);

    auto time1 = chrono::steady_clock::now();

    // Launch the CUDA kernel
    convolutionKernel <<<gridSize, blockSize>>> (dev_f, dev_c, dev_v, width, height);

    // Copy the result back to the host
    cudaMemcpy(v, dev_v, width * height * sizeof(int), cudaMemcpyDeviceToHost);

    auto time2 = chrono::steady_clock::now();
    auto diff = time2 - time1;
    cout << chrono::duration <double, milli>(diff).count();

    // Clean up
    cudaFree(dev_f);
    cudaFree(dev_c);
    cudaFree(dev_v);

    writeResultToFile(height, width, v);

    delete[] f;
    delete[] v;

    

    return 0;
}


