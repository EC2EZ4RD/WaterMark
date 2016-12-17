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

Y = tmpim(:,:,1);
[x0,y0] = size(Y);
p = 1;
for x =1:x0
    for y = 1:y0
        Y(x,y) = Y(x,y)-mod(Y(x,y),2) + uint8(watermark(p,1));
        if p==len
            break;
        end
        p=p+1;
    end
    if p == len
        break;
    end
end
tmpim(:,:,1) = Y;
%im(:,:,1)
tmpim = ntsc2rgb(tmpim);
%im(:,:,1)
watermark = reshape(watermark,xmark,[]);
figure;imshow(tmpim)
imwrite(tmpim,'markedImg.bmp','bmp');
figure;imshow(watermark);