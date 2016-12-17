clear;
clc;
im = imread('lena.jpg');

watermark = imread('dong.jpg');
watermark = rgb2gray(watermark);
watermark = im2bw(watermark,graythresh(watermark));
[xmark,ymark] = size(watermark);
watermark = watermark(:);
[len,~]=size(watermark);

tmpim = rgb2ntsc(im);
%im(:,:,1)
tmpim = ceil(tmpim.*256);
tmpim = uint8(tmpim);
%此时tmpim为一个uint8的yiq矩阵
Y = tmpim(:,:,1);
[cA0,cH0,cV0,cD0] = dwt2(Y,'db1');
tmpcA = uint16(cA0);
[x0,y0] = size(tmpcA);
p = 1;
for x =1:x0
    for y = 1:y0
        tmpcA(x,y) = tmpcA(x,y)-mod(tmpcA(x,y),2) + uint16(watermark(p,1));
        if p==len
            break;
        end
        p=p+1;
    end
    if p == len
        break;
    end
end
tmpcA = double(tmpcA);
Y = idwt2(tmpcA,cH0,cV0,cD0,'db1');
tmpim(:,:,1) = Y;
%im(:,:,1)
tmpim = ntsc2rgb(tmpim);
%im(:,:,1)
%watermark = reshape(watermark,xmark,[]);
figure;imshow(tmpim)
imwrite(tmpim,'markedImg.bmp','bmp');
%figure;imshow(watermark);