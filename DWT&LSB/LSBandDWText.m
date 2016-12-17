clear;
clc;
im = imread('markedImg.bmp');
%im(:,:,1)
len = 1024;
tmpim = ceil(rgb2ntsc(im).*256);

watermark = zeros(len,1);
Y = tmpim(:,:,1);
[cA,cH,cV,cD] = dwt2(Y,'db1');
tmpcA = uint16(cA);
[x0,y0] = size(tmpcA);
p = 1;
for x =1:x0
    for y = 1:y0
        watermark(p,1) = mod(tmpcA(x,y),2);
        if p==len
            break;
        end
        p=p+1;
    end
    if p == len
        break;
    end
end
watermark = reshape(~watermark,32,[]);
imshow(watermark);