import numpy as np

def parse_sever_file(file):
    data = np.loadtxt(file)
    return data[:,0].sum()/(data.shape[0]/2)

def parse_query_file(file):
    data = np.loadtxt(file)
    return data.sum()/(data.shape[0])


if __name__ == "__main__":
    file1 = input("file path of server time :")
    file2 = input("file path of query time :")
    print(f"server time: {parse_sever_file(file1)/1000000}")
    print(f"query time: {parse_query_file(file2)/1000000}")
