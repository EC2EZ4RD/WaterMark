clear;
clc;
%im = imread('markedImg.bmp');
%im = imread('whitenoise.bmp');
%im = imread('rotate.bmp');
%im = imread('cutpart.bmp');
im = imread('gaussian.bmp');
[x0,y0] = size(im);
xmark = 32;
watermark = zeros(xmark,xmark);


tmpim = rgb2ntsc(im);
%im(:,:,1)
tmpim = ceil(tmpim.*256);
tmpim = uint8(tmpim);
%此时tmpim为一个uint8的yiq矩阵
Y = tmpim(:,:,1);
[cA0,cH0,cV0,cD0] = dwt2(Y,'db1');
[xcA,~] = size(cA0);
blocksize = floor(xcA/xmark);
delta = 105;

for i = 1:blocksize:xcA
    for j = 1:blocksize:xcA
        [U,S,V] = svd(cA0(i:i+blocksize-1,j:j+blocksize-1));
        [xs,ys] = find(S==max(max(S)));
        ga = floor(S(xs,ys)/delta);
        if mod(ga,2) == 0
            watermark(ceil(i/blocksize),ceil(j/blocksize)) = 0;
        else
            watermark(ceil(i/blocksize),ceil(j/blocksize)) = 1;
        end
    end
end
imshow(watermark);